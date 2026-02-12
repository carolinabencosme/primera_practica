package org.example.primera_practica.service.impl;

import org.example.primera_practica.dto.ProjectDTO;
import org.example.primera_practica.model.Project;
import org.example.primera_practica.model.Role;
import org.example.primera_practica.model.RoleType;
import org.example.primera_practica.model.User;
import org.example.primera_practica.repository.ProjectRepository;
import org.example.primera_practica.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplAuthorizationTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User owner;
    private User otherUser;
    private User admin;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = buildUser("owner", RoleType.ROLE_USER);
        otherUser = buildUser("other", RoleType.ROLE_USER);
        admin = buildUser("admin", RoleType.ROLE_ADMIN);

        project = new Project();
        project.setId(100L);
        project.setName("Project Owner");
        project.setCreatedBy(owner);
    }

    @Test
    void getProjectByIdForUser_deniesAccessForDifferentUser() {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> projectService.getProjectByIdForUser(100L, "other"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void getProjectByIdForUser_allowsAdminAccess() {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        ProjectDTO dto = projectService.getProjectByIdForUser(100L, "admin");

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getCreatedBy()).isEqualTo("owner");
    }

    @Test
    void deleteProjectForUser_deniesDifferentUserAndAllowsAdmin() {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> projectService.deleteProjectForUser(100L, "other"))
                .isInstanceOf(AccessDeniedException.class);
        verify(projectRepository, never()).delete(any(Project.class));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        projectService.deleteProjectForUser(100L, "admin");

        verify(projectRepository, times(1)).delete(project);
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
