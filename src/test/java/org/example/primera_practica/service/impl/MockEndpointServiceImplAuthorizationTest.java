package org.example.primera_practica.service.impl;

import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.model.*;
import org.example.primera_practica.repository.MockEndpointRepository;
import org.example.primera_practica.repository.ProjectRepository;
import org.example.primera_practica.repository.UserRepository;
import org.example.primera_practica.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockEndpointServiceImplAuthorizationTest {

    @Mock
    private MockEndpointRepository mockEndpointRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private MockEndpointServiceImpl mockEndpointService;

    private User owner;
    private User otherUser;
    private User admin;
    private Project project;
    private MockEndpoint mockEndpoint;

    @BeforeEach
    void setUp() {
        owner = buildUser("owner", RoleType.ROLE_USER);
        otherUser = buildUser("other", RoleType.ROLE_USER);
        admin = buildUser("admin", RoleType.ROLE_ADMIN);

        project = new Project();
        project.setId(200L);
        project.setName("Owner project");
        project.setCreatedBy(owner);

        mockEndpoint = new MockEndpoint();
        mockEndpoint.setId(300L);
        mockEndpoint.setName("Mock 1");
        mockEndpoint.setPath("/api/test");
        mockEndpoint.setMethod(HttpMethod.GET);
        mockEndpoint.setHttpStatusCode(200);
        mockEndpoint.setContentType("application/json");
        mockEndpoint.setResponseBody("{}");
        mockEndpoint.setExpirationDate(LocalDateTime.now().plusDays(1));
        mockEndpoint.setDelaySeconds(0);
        mockEndpoint.setRequiresJwt(false);
        mockEndpoint.setCreatedBy(owner);
        mockEndpoint.setProject(project);
        mockEndpoint.setHeaders(List.of());
    }

    @Test
    void getMockEndpointByIdForUser_deniesDifferentUserAndAllowsAdmin() {
        when(mockEndpointRepository.findById(300L)).thenReturn(Optional.of(mockEndpoint));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> mockEndpointService.getMockEndpointByIdForUser(300L, "other"))
                .isInstanceOf(AccessDeniedException.class);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        MockEndpointDTO dto = mockEndpointService.getMockEndpointByIdForUser(300L, "admin");
        assertThat(dto.getId()).isEqualTo(300L);
    }

    @Test
    void updateAndDeleteMockForUser_deniesDifferentUserAndAllowsAdmin() {
        when(mockEndpointRepository.findById(300L)).thenReturn(Optional.of(mockEndpoint));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

        MockEndpointDTO update = new MockEndpointDTO();
        update.setName("Updated by unauthorized user");

        assertThatThrownBy(() -> mockEndpointService.updateMockEndpointForUser(300L, update, "other"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> mockEndpointService.deleteMockEndpointForUser(300L, "other"))
                .isInstanceOf(AccessDeniedException.class);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(mockEndpointRepository.save(any(MockEndpoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockEndpointDTO updated = mockEndpointService.updateMockEndpointForUser(300L, update, "admin");
        assertThat(updated.getName()).isEqualTo("Updated by unauthorized user");

        mockEndpointService.deleteMockEndpointForUser(300L, "admin");
        verify(mockEndpointRepository).delete(mockEndpoint);
    }

    @Test
    void getAllMockEndpointsByProjectForUser_deniesDifferentUserAndAllowsAdmin() {
        when(projectRepository.findById(200L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> mockEndpointService.getAllMockEndpointsByProjectForUser(200L, "other"))
                .isInstanceOf(AccessDeniedException.class);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(mockEndpointRepository.findByProject(project)).thenReturn(List.of(mockEndpoint));

        List<MockEndpointDTO> result = mockEndpointService.getAllMockEndpointsByProjectForUser(200L, "admin");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(300L);
    }

    private User buildUser(String username, RoleType roleType) {
        User user = new User();
        user.setUsername(username);

        Role role = new Role();
        role.setName(roleType);
        user.setRoles(Set.of(role));

        return user;
    }
}
