# Testing Documentation

This document describes the testing strategy, test types, and guidelines for the Mockup API Server.

## Table of Contents

- [Overview](#overview)
- [Testing Strategy](#testing-strategy)
- [Test Types](#test-types)
- [Running Tests](#running-tests)
- [Test Coverage](#test-coverage)
- [Key Test Cases](#key-test-cases)
- [Writing Tests](#writing-tests)
- [Testing Best Practices](#testing-best-practices)

## Overview

The Mockup API Server uses **JUnit 5** and **Spring Boot Test** for automated testing. The current implementation includes basic integration tests, with room for expansion to comprehensive unit, integration, and end-to-end testing.

### Testing Framework

| Component | Technology |
|-----------|-----------|
| Test Framework | JUnit 5 (Jupiter) |
| Spring Testing | Spring Boot Test |
| Assertions | AssertJ, Hamcrest |
| Mocking | Mockito |
| Test Runner | Gradle Test |

---

## Testing Strategy

### Testing Pyramid

```
           ┌─────────────┐
           │     E2E     │  ← Few (Manual/Automated UI tests)
           └─────────────┘
         ┌─────────────────┐
         │   Integration   │  ← Some (API, Service, Repository)
         └─────────────────┘
       ┌─────────────────────┐
       │      Unit Tests      │  ← Many (Service, Util, Validation)
       └─────────────────────┘
```

### Test Levels

1. **Unit Tests** (Fast, Isolated)
   - Service layer logic
   - Utility methods
   - Validation logic
   - No database, no Spring context

2. **Integration Tests** (Moderate Speed)
   - Repository tests with database
   - Service tests with repositories
   - Controller tests with MockMvc
   - Full Spring context

3. **End-to-End Tests** (Slow, Comprehensive)
   - Full application flow
   - Real HTTP requests
   - Database persistence
   - Authentication/authorization

---

## Test Types

### 1. Unit Tests

**Purpose**: Test individual components in isolation

**Characteristics:**
- No Spring context
- Use Mockito for dependencies
- Fast execution (<10ms per test)
- No database or external resources

**Example: Service Unit Test**

```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ProjectServiceImpl projectService;
    
    @Test
    void createProject_shouldSaveProject() {
        // Given
        ProjectDTO dto = new ProjectDTO("UserAPI", "Description");
        User user = new User("admin");
        Project project = new Project("UserAPI", "Description", user);
        
        when(userRepository.findByUsername("admin"))
            .thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class)))
            .thenReturn(project);
        
        // When
        ProjectDTO result = projectService.createProject(dto, "admin");
        
        // Then
        assertThat(result.getName()).isEqualTo("UserAPI");
        verify(projectRepository).save(any(Project.class));
    }
    
    @Test
    void createProject_duplicateName_shouldThrowException() {
        // Given
        ProjectDTO dto = new ProjectDTO("ExistingProject", "Description");
        when(projectRepository.findByName("ExistingProject"))
            .thenReturn(Optional.of(new Project()));
        
        // When/Then
        assertThatThrownBy(() -> projectService.createProject(dto, "admin"))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Project with name ExistingProject already exists");
    }
}
```

---

### 2. Repository Tests

**Purpose**: Test data access layer with real database

**Characteristics:**
- Uses `@DataJpaTest`
- In-memory H2 database
- Transactional (rollback after each test)
- Auto-configuration of JPA

**Example: Repository Test**

```java
@DataJpaTest
class ProjectRepositoryTest {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void findByName_existingProject_shouldReturnProject() {
        // Given
        User user = new User("testuser", "password", "test@example.com");
        entityManager.persist(user);
        
        Project project = new Project("TestAPI", "Description", user);
        entityManager.persist(project);
        entityManager.flush();
        
        // When
        Optional<Project> found = projectRepository.findByName("TestAPI");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TestAPI");
        assertThat(found.get().getCreatedBy().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    void findByName_nonExistent_shouldReturnEmpty() {
        // When
        Optional<Project> found = projectRepository.findByName("NonExistent");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void deleteProject_shouldCascadeToMockEndpoints() {
        // Given
        User user = new User("testuser", "password", "test@example.com");
        entityManager.persist(user);
        
        Project project = new Project("TestAPI", "Description", user);
        entityManager.persist(project);
        
        MockEndpoint endpoint = new MockEndpoint(
            "/users", HttpMethod.GET, 200, "{}", project, user
        );
        entityManager.persist(endpoint);
        entityManager.flush();
        
        Long projectId = project.getId();
        
        // When
        projectRepository.deleteById(projectId);
        entityManager.flush();
        
        // Then
        assertThat(projectRepository.findById(projectId)).isEmpty();
        // Mock endpoint should also be deleted (cascade)
    }
}
```

---

### 3. Service Integration Tests

**Purpose**: Test service layer with real dependencies

**Characteristics:**
- Uses `@SpringBootTest`
- Full Spring context
- Real database interactions
- Transactional

**Example: Service Integration Test**

```java
@SpringBootTest
@Transactional
class MockEndpointServiceIntegrationTest {
    
    @Autowired
    private MockEndpointService mockEndpointService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private Project testProject;
    
    @BeforeEach
    void setup() {
        testUser = new User("testuser", "password", "test@example.com");
        testUser = userRepository.save(testUser);
        
        ProjectDTO projectDTO = new ProjectDTO("TestAPI", "Description");
        testProject = projectService.createProject(projectDTO, "testuser");
    }
    
    @Test
    void createMockEndpoint_shouldPersist() {
        // Given
        MockEndpointDTO dto = new MockEndpointDTO();
        dto.setPath("/users/1");
        dto.setMethod(HttpMethod.GET);
        dto.setHttpStatusCode(200);
        dto.setResponseBody("{\"id\": 1}");
        dto.setProjectId(testProject.getId());
        
        // When
        MockEndpointDTO result = mockEndpointService
            .createMockEndpoint(dto, "testuser");
        
        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getPath()).isEqualTo("/users/1");
        
        // Verify database
        MockEndpointDTO fetched = mockEndpointService
            .getMockEndpointById(result.getId());
        assertThat(fetched.getResponseBody()).isEqualTo("{\"id\": 1}");
    }
}
```

---

### 4. Controller Tests (MockMvc)

**Purpose**: Test REST API endpoints without full server

**Characteristics:**
- Uses `@WebMvcTest`
- MockMvc for HTTP requests
- Mock service dependencies
- No server startup

**Example: Controller Test**

```java
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProjectService projectService;
    
    @MockBean
    private UserDetailsService userDetailsService;
    
    @MockBean
    private JwtService jwtService;
    
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void listProjects_shouldReturnView() throws Exception {
        // Given
        List<ProjectDTO> projects = Arrays.asList(
            new ProjectDTO("API1", "Description 1"),
            new ProjectDTO("API2", "Description 2")
        );
        when(projectService.getProjectsByUser("testuser"))
            .thenReturn(projects);
        
        // When/Then
        mockMvc.perform(get("/projects"))
            .andExpect(status().isOk())
            .andExpect(view().name("projects/list"))
            .andExpect(model().attribute("projects", hasSize(2)))
            .andExpect(model().attribute("projects", hasItem(
                hasProperty("name", is("API1"))
            )));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createProject_validInput_shouldRedirect() throws Exception {
        // Given
        ProjectDTO savedProject = new ProjectDTO("NewAPI", "Description");
        savedProject.setId(1L);
        when(projectService.createProject(any(), eq("testuser")))
            .thenReturn(savedProject);
        
        // When/Then
        mockMvc.perform(post("/projects")
                .param("name", "NewAPI")
                .param("description", "Description")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/projects"));
    }
}
```

---

### 5. End-to-End Tests

**Purpose**: Test complete user flows

**Characteristics:**
- Uses `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- TestRestTemplate or WebTestClient
- Full application startup
- Real HTTP requests

**Example: E2E Test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MockApiE2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private MockEndpointRepository mockEndpointRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setup() {
        // Create test data
        User user = new User("testuser", "password", "test@example.com");
        user = userRepository.save(user);
        
        Project project = new Project("E2ETestAPI", "E2E Testing", user);
        project = projectRepository.save(project);
        
        MockEndpoint endpoint = new MockEndpoint();
        endpoint.setPath("/test");
        endpoint.setMethod(HttpMethod.GET);
        endpoint.setHttpStatusCode(200);
        endpoint.setResponseBody("{\"status\": \"ok\"}");
        endpoint.setContentType("application/json");
        endpoint.setProject(project);
        endpoint.setCreatedBy(user);
        mockEndpointRepository.save(endpoint);
    }
    
    @Test
    void executeMock_shouldReturnConfiguredResponse() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/mock/E2ETestAPI/test",
            String.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"status\": \"ok\"}");
        assertThat(response.getHeaders().getContentType())
            .isEqualTo(MediaType.APPLICATION_JSON);
    }
    
    @Test
    void executeMock_nonExistent_shouldReturn404() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/mock/E2ETestAPI/nonexistent",
            String.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

---

### 6. Security Tests

**Purpose**: Test authentication and authorization

**Example: Security Test**

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void accessProtectedPage_notAuthenticated_shouldRedirect() throws Exception {
        mockMvc.perform(get("/projects"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }
    
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void accessAdminPage_asUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void accessAdminPage_asAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk());
    }
}
```

---

## Running Tests

### Using Gradle

**Run All Tests:**
```bash
./gradlew test
```

**Run Tests in Watch Mode:**
```bash
./gradlew test --continuous
```

**Run Specific Test Class:**
```bash
./gradlew test --tests ProjectServiceImplTest
```

**Run Specific Test Method:**
```bash
./gradlew test --tests ProjectServiceImplTest.createProject_shouldSaveProject
```

**Run Tests with Coverage:**
```bash
./gradlew test jacocoTestReport
```

**View Test Report:**
```bash
open build/reports/tests/test/index.html
```

---

### Using IDE

**IntelliJ IDEA:**
1. Right-click on test class or method
2. Select "Run 'TestName'"
3. Or use `Ctrl+Shift+F10` (Windows/Linux) or `Cmd+Shift+R` (Mac)

**Eclipse:**
1. Right-click on test class
2. Select "Run As" → "JUnit Test"

---

### Continuous Integration

**GitHub Actions Example:**

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

---

## Test Coverage

### Current Coverage

**As of v1.0.0:**

| Module | Coverage | Status |
|--------|----------|--------|
| Overall | ~5% | ⚠️ Low |
| Controllers | 0% | ❌ None |
| Services | 0% | ❌ None |
| Repositories | 0% | ❌ None |
| Entities | N/A | - |

**Current Tests:**
- ✅ `PrimeraPracticaApplicationTests.contextLoads()` - Basic context test

### Coverage Goals

**Target Coverage:**

| Module | Target | Priority |
|--------|--------|----------|
| Services | 80% | High |
| Controllers | 70% | High |
| Repositories | 60% | Medium |
| Utilities | 90% | Medium |
| Overall | 75% | - |

---

### Measuring Coverage

**JaCoCo Plugin:**

```gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.10"
}

jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

test {
    finalizedBy jacocoTestReport
}
```

**Generate Report:**
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## Key Test Cases

### Priority Test Scenarios

#### 1. Mock Endpoint Execution

**Critical Paths:**
- ✅ Execute GET mock endpoint → Returns configured response
- ✅ Execute POST mock endpoint → Returns configured response
- ✅ Mock with custom headers → Headers present in response
- ✅ Mock with delay → Response delayed correctly
- ✅ Expired mock → Returns 410 GONE
- ✅ JWT-protected mock without token → Returns 401
- ✅ JWT-protected mock with valid token → Returns 200
- ✅ JWT-protected mock with expired token → Returns 401
- ✅ Non-existent mock → Returns 404

---

#### 2. Project Management

**Test Cases:**
- ✅ Create project with valid data → Success
- ✅ Create project with duplicate name → DuplicateResourceException
- ✅ Create project with blank name → ValidationException
- ✅ Update project → Changes persisted
- ✅ Delete project → Project and endpoints deleted
- ✅ List user's projects → Only user's projects returned

---

#### 3. Mock Endpoint Management

**Test Cases:**
- ✅ Create mock endpoint → Success
- ✅ Create mock with invalid path → ValidationException
- ✅ Create mock without project → ValidationException
- ✅ Update mock response → Changes reflected
- ✅ Delete mock endpoint → Endpoint removed
- ✅ Add custom headers → Headers saved
- ✅ Remove custom headers → Headers deleted

---

#### 4. Authentication & Authorization

**Test Cases:**
- ✅ Login with valid credentials → Success
- ✅ Login with invalid password → Failure
- ✅ Login with non-existent user → Failure
- ✅ Access protected page without login → Redirect to login
- ✅ Access admin page as user → 403 Forbidden
- ✅ Access admin page as admin → Success
- ✅ JWT generation → Valid token created
- ✅ JWT validation → Token verified correctly
- ✅ Expired JWT → Rejected

---

#### 5. Data Validation

**Test Cases:**
- ✅ Valid project DTO → No errors
- ✅ Invalid project name (blank) → Validation error
- ✅ Invalid email format → Validation error
- ✅ Password too short → Validation error
- ✅ Negative status code → Validation error

---

## Writing Tests

### Test Naming Convention

**Pattern:** `methodName_scenario_expectedBehavior`

**Examples:**
```java
createProject_validInput_shouldSaveProject()
createProject_duplicateName_shouldThrowException()
executeMock_expiredEndpoint_shouldReturn410()
login_invalidPassword_shouldFail()
```

---

### AAA Pattern (Arrange, Act, Assert)

```java
@Test
void testExample() {
    // Arrange (Given)
    ProjectDTO dto = new ProjectDTO("API", "Description");
    when(repository.save(any())).thenReturn(new Project());
    
    // Act (When)
    ProjectDTO result = service.createProject(dto);
    
    // Assert (Then)
    assertThat(result.getName()).isEqualTo("API");
    verify(repository).save(any());
}
```

---

### Assertions

**AssertJ (Recommended):**
```java
assertThat(result).isNotNull();
assertThat(result.getName()).isEqualTo("API");
assertThat(result.getId()).isPositive();
assertThat(list).hasSize(3).contains(item);
assertThatThrownBy(() -> service.method())
    .isInstanceOf(Exception.class)
    .hasMessageContaining("error");
```

**JUnit 5:**
```java
assertEquals("API", result.getName());
assertTrue(result.getId() > 0);
assertThrows(DuplicateResourceException.class, () -> service.method());
```

---

### Mocking

**Mockito:**
```java
@Mock
private ProjectRepository repository;

@Test
void test() {
    // Stub method
    when(repository.findById(1L)).thenReturn(Optional.of(new Project()));
    
    // Verify interaction
    verify(repository).save(any(Project.class));
    verify(repository, times(2)).findAll();
    verify(repository, never()).delete(any());
}
```

---

## Testing Best Practices

### 1. Test Independence

**DO:**
- ✅ Each test should be independent
- ✅ Use `@BeforeEach` to set up test data
- ✅ Clean up resources in `@AfterEach`
- ✅ Don't rely on test execution order

**DON'T:**
- ❌ Share mutable state between tests
- ❌ Depend on other tests' side effects

---

### 2. Test Data

**DO:**
- ✅ Use meaningful test data
- ✅ Create test data factories/builders
- ✅ Use constants for magic values

**Example:**
```java
class TestDataFactory {
    public static User createUser(String username) {
        return new User(username, "password", username + "@test.com");
    }
    
    public static Project createProject(String name, User user) {
        return new Project(name, "Test description", user);
    }
}
```

---

### 3. Avoid Over-Mocking

**Prefer Real Objects:**
```java
// Good: Use real DTO
ProjectDTO dto = new ProjectDTO("API", "Description");

// Avoid: Mocking simple objects
ProjectDTO dto = mock(ProjectDTO.class);
when(dto.getName()).thenReturn("API");
```

---

### 4. Test Edge Cases

**Consider:**
- Null values
- Empty collections
- Boundary values (min, max)
- Special characters in strings
- Concurrent access
- Large data sets

---

### 5. Readable Tests

**Use descriptive names and comments:**
```java
@Test
void createProject_nameWithSpecialCharacters_shouldFail() {
    // Given: Project name contains invalid characters
    ProjectDTO dto = new ProjectDTO("API@#$", "Description");
    
    // When: Attempting to create project
    // Then: Should throw validation exception
    assertThatThrownBy(() -> service.createProject(dto))
        .isInstanceOf(ValidationException.class);
}
```

---

## Conclusion

Comprehensive testing is crucial for application reliability. This document provides the foundation for building a robust test suite. As the application evolves, expand test coverage to include all critical paths and edge cases.

**Next Steps:**
1. Increase unit test coverage to 80%+
2. Add integration tests for all services
3. Implement E2E tests for critical user flows
4. Set up CI/CD with automated testing
5. Monitor test coverage trends
