# System Architecture

This document describes the architecture, design patterns, and technical decisions of the Mockup API Server.

## Table of Contents

- [Overview](#overview)
- [Layered Architecture](#layered-architecture)
- [Component Diagram](#component-diagram)
- [Data Flow](#data-flow)
- [Design Patterns](#design-patterns)
- [Technology Stack](#technology-stack)
- [Design Decisions](#design-decisions)

## Overview

The Mockup API Server follows a **layered architecture** pattern based on Spring Boot best practices. It separates concerns into distinct layers, promoting maintainability, testability, and scalability.

### Key Architectural Principles

1. **Separation of Concerns**: Each layer has a specific responsibility
2. **Dependency Inversion**: High-level modules depend on abstractions (interfaces)
3. **Single Responsibility**: Each class has one reason to change
4. **Open/Closed Principle**: Open for extension, closed for modification

## Layered Architecture

The application consists of 6 main layers:

```
┌─────────────────────────────────────────────────┐
│         Presentation Layer                      │
│  (Controllers: Web UI + REST API)               │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│         Security Layer                          │
│  (Filters, JWT, Authentication)                 │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│         Service Layer                           │
│  (Business Logic, Interfaces)                   │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│         Repository Layer                        │
│  (Data Access, JPA Repositories)                │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│         Persistence Layer                       │
│  (JPA Entities, Database)                       │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│         Database (H2)                           │
└─────────────────────────────────────────────────┘
```

### Layer Descriptions

#### 1. Presentation Layer

**Responsibilities:**
- Handle HTTP requests and responses
- Input validation
- View rendering (Thymeleaf)
- DTO transformation

**Components:**
- **Web Controllers**: MVC controllers for UI (`HomeController`, `ProjectController`, `MockEndpointController`, `UserController`)
- **API Controllers**: REST endpoints (`MockApiController`)
- **DTOs**: Data transfer objects for request/response

**Technologies:**
- Spring MVC
- Thymeleaf
- Jakarta Bean Validation

#### 2. Security Layer

**Responsibilities:**
- Authentication and authorization
- JWT token generation and validation
- Security filter chain
- Access control

**Components:**
- `SecurityConfig`: Security configuration
- `JwtAuthenticationFilter`: JWT token extraction and validation
- `UserDetailsServiceImpl`: User authentication
- `JwtService`: JWT utilities

**Technologies:**
- Spring Security 6
- JJWT (Java JWT library)

#### 3. Service Layer

**Responsibilities:**
- Business logic implementation
- Transaction management
- Data validation
- Coordination between repositories

**Components:**
- `UserService` / `UserServiceImpl`
- `ProjectService` / `ProjectServiceImpl`
- `MockEndpointService` / `MockEndpointServiceImpl`
- `JwtService` / `JwtServiceImpl`

**Design Pattern:**
- Interface-based design for flexibility and testability
- `@Transactional` for ACID operations

#### 4. Repository Layer

**Responsibilities:**
- Data access abstraction
- Query execution
- Database operations

**Components:**
- `UserRepository`
- `RoleRepository`
- `ProjectRepository`
- `MockEndpointRepository`

**Technologies:**
- Spring Data JPA
- Query methods and custom queries

#### 5. Persistence Layer

**Responsibilities:**
- Entity mapping
- Relationship management
- Database constraints

**Components:**
- JPA Entities: `User`, `Role`, `Project`, `MockEndpoint`, `MockHeader`
- Enums: `RoleType`, `HttpMethod`

**Technologies:**
- JPA/Hibernate
- Lombok for boilerplate reduction

#### 6. Database

**Technology:** H2 Database (in-memory/file-based)

**Configuration:**
- In-memory for development: `jdbc:h2:mem:mockdb`
- Auto-DDL: `update` mode
- Console enabled for development

## Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Client (Browser/API)                     │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
               │ HTTP                         │ HTTP
               │                              │
      ┌────────▼─────────┐           ┌───────▼──────────┐
      │  Web Controllers │           │  API Controllers  │
      │  (MVC)           │           │  (REST)           │
      └────────┬─────────┘           └───────┬──────────┘
               │                              │
               └──────────────┬───────────────┘
                              │
                   ┌──────────▼──────────┐
                   │  Security Filter    │
                   │  (JWT Auth)         │
                   └──────────┬──────────┘
                              │
               ┌──────────────┼──────────────┐
               │              │              │
       ┌───────▼──────┐ ┌────▼─────┐ ┌─────▼────────┐
       │ UserService  │ │ Project  │ │ MockEndpoint │
       │              │ │ Service  │ │ Service      │
       └───────┬──────┘ └────┬─────┘ └─────┬────────┘
               │              │              │
       ┌───────▼──────┐ ┌────▼─────┐ ┌─────▼────────┐
       │     User     │ │ Project  │ │ MockEndpoint │
       │  Repository  │ │Repository│ │ Repository   │
       └───────┬──────┘ └────┬─────┘ └─────┬────────┘
               │              │              │
               └──────────────┼──────────────┘
                              │
                   ┌──────────▼──────────┐
                   │    H2 Database      │
                   │    (JPA/Hibernate)  │
                   └─────────────────────┘
```

## Data Flow

### 1. Web Request Flow (CRUD Operations)

```
User → Browser → Web Controller → Service → Repository → Database
                      ↓
                 Thymeleaf
                      ↓
                   HTML Response
```

**Example: Create Project**
1. User submits form at `/projects/new`
2. `ProjectController.saveProject()` receives `ProjectDTO`
3. Validates input (Bean Validation)
4. `ProjectService.createProject()` processes business logic
5. `ProjectRepository.save()` persists entity
6. Redirects to project list view
7. Thymeleaf renders updated page

### 2. Mock API Execution Flow

```
Client → MockApiController → Validate → MockEndpointService
              ↓                           ↓
         Extract Path                Find Endpoint
              ↓                           ↓
         Parse Project              Check Expiration
              ↓                           ↓
       JWT Validation (if required)   Apply Delay
              ↓                           ↓
         Build Response             Return Mock Data
```

**Example: Execute Mock GET /api/mock/UserAPI/users/1**

1. **Request arrives**: `GET /api/mock/UserAPI/users/1`
2. **Extract components**:
   - Project name: `UserAPI`
   - Path: `/users/1`
   - Method: `GET`
3. **Find endpoint**: Query `MockEndpointRepository` for matching record
4. **Validate**:
   - Check if endpoint exists (404 if not)
   - Check expiration date (410 GONE if expired)
   - Validate JWT if `requiresJwt=true` (401 if invalid)
5. **Apply delay**: `Thread.sleep(delaySeconds * 1000)`
6. **Build response**:
   - Set status code
   - Set Content-Type header
   - Add custom headers from `MockHeader` entities
   - Set response body
7. **Return** mock response to client

### 3. JWT Authentication Flow

```
Login → Authenticate → Generate JWT → Return Token
   ↓                                        ↓
UserDetailsService                    Client stores
   ↓                                        ↓
Database                             Subsequent requests
                                            ↓
                                    Include: Authorization: Bearer <token>
                                            ↓
                                    JwtAuthenticationFilter
                                            ↓
                                    Validate & Set SecurityContext
```

## Design Patterns

### 1. Model-View-Controller (MVC)

**Purpose**: Separate presentation, business logic, and data

**Implementation:**
- **Model**: JPA entities (`User`, `Project`, `MockEndpoint`)
- **View**: Thymeleaf templates (`projects/list.html`, `mocks/form.html`)
- **Controller**: Spring MVC controllers (`ProjectController`, `MockEndpointController`)

### 2. Repository Pattern

**Purpose**: Abstract data access logic

**Implementation:**
- Spring Data JPA repositories
- Interface-based design
- Automatic query derivation

```java
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
}
```

### 3. Service Layer Pattern

**Purpose**: Encapsulate business logic

**Implementation:**
- Service interfaces define contracts
- Implementation classes contain logic
- `@Transactional` for data consistency

```java
public interface ProjectService {
    ProjectDTO createProject(ProjectDTO projectDTO);
    List<ProjectDTO> getAllProjects();
}
```

### 4. Data Transfer Object (DTO)

**Purpose**: Decouple API contracts from domain models

**Implementation:**
- DTOs for request/response payloads
- Separate from entities to control exposure
- Validation annotations on DTOs

```java
public class ProjectDTO {
    @NotBlank
    private String name;
    private String description;
}
```

### 5. Filter Pattern

**Purpose**: Pre-process requests for cross-cutting concerns

**Implementation:**
- `JwtAuthenticationFilter` extends `OncePerRequestFilter`
- Intercepts requests to validate JWT
- Sets `SecurityContext` for authenticated users

### 6. Dependency Injection

**Purpose**: Loose coupling and testability

**Implementation:**
- Spring's IoC container
- Constructor injection (preferred)
- `@Autowired` for dependency resolution

### 7. Strategy Pattern (Implicit)

**Purpose**: Different mock execution strategies

**Implementation:**
- HTTP method-based routing
- Response type handling
- Conditional JWT validation

## Technology Stack

### Backend Framework

**Spring Boot 4.0.2**
- Rapid application development
- Auto-configuration
- Embedded server (Tomcat)
- Production-ready features (health checks, metrics)

### Persistence

**Spring Data JPA + Hibernate**
- Object-relational mapping
- Automatic schema generation
- Query derivation from method names
- Transaction management

**H2 Database**
- Lightweight, embedded
- In-memory or file-based
- Perfect for development and testing
- Web console for debugging

### Security

**Spring Security 6**
- Authentication and authorization
- Filter-based security
- BCrypt password encoding
- CSRF protection

**JJWT 0.12.6**
- JWT token generation
- Signature validation
- Claims management
- Secure key handling

### View Layer

**Thymeleaf**
- Server-side HTML rendering
- Natural templates (valid HTML)
- Spring integration
- Expression language

### Utilities

**Lombok**
- Reduce boilerplate code
- `@Data`, `@Builder`, `@Slf4j`
- Compile-time code generation

**Spring DevTools**
- Hot reload during development
- Automatic restart
- LiveReload support

## Design Decisions

### 1. H2 Database for Development

**Decision**: Use H2 in-memory database

**Rationale:**
- Fast startup and teardown
- No external dependencies
- Easy debugging with web console
- Suitable for prototype and testing

**Trade-offs:**
- Not for production (use PostgreSQL, MySQL)
- Data lost on restart (in-memory mode)

### 2. JWT for Stateless Authentication

**Decision**: Implement JWT for API authentication

**Rationale:**
- Stateless (no server-side session storage)
- Scalable (no session affinity)
- Mobile-friendly
- Decoupled from form-based login

**Trade-offs:**
- Token invalidation requires additional logic
- Larger payload than session cookies
- Secret key management critical

### 3. Interface-Based Services

**Decision**: Define service layer as interfaces

**Rationale:**
- Testability (easy to mock)
- Flexibility (swap implementations)
- Follows SOLID principles
- Spring proxy support

**Trade-offs:**
- More files to maintain
- Slight complexity increase

### 4. Path-Based Mock Routing

**Decision**: Use `/api/mock/{projectName}/**` pattern

**Rationale:**
- Logical grouping by project
- Supports arbitrary paths
- Flexible routing
- Clear API structure

**Trade-offs:**
- Path parsing complexity
- Potential name collisions

### 5. Dynamic Response Execution

**Decision**: Execute mocks from database, not code

**Rationale:**
- No redeployment for new mocks
- User-configurable at runtime
- Flexible response options
- Supports expiration and delays

**Trade-offs:**
- Slight performance overhead (database lookup)
- Limited to stored data (no dynamic logic)

### 6. Role-Based Access Control

**Decision**: Implement ADMIN and USER roles

**Rationale:**
- Clear permission separation
- Standard Spring Security pattern
- Extensible for future roles
- Protect sensitive operations

**Trade-offs:**
- All-or-nothing permissions
- No fine-grained control (could add later)

### 7. Thymeleaf for Server-Side Rendering

**Decision**: Use Thymeleaf instead of SPA framework

**Rationale:**
- Simpler architecture
- Server-side validation
- SEO-friendly
- No separate frontend build

**Trade-offs:**
- Less interactive than React/Angular
- Full page reloads
- Limited real-time features

## Conclusion

The Mockup API Server architecture prioritizes **simplicity**, **maintainability**, and **extensibility**. The layered approach with clear separation of concerns allows for easy testing, modification, and future enhancements. The technology choices balance modern best practices with practical development efficiency.
