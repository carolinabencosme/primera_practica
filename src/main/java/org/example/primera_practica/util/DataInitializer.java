package org.example.primera_practica.util;

import org.example.primera_practica.model.HttpMethod;
import org.example.primera_practica.model.MockEndpoint;
import org.example.primera_practica.model.Project;
import org.example.primera_practica.model.Role;
import org.example.primera_practica.model.RoleType;
import org.example.primera_practica.model.User;
import org.example.primera_practica.repository.MockEndpointRepository;
import org.example.primera_practica.repository.ProjectRepository;
import org.example.primera_practica.repository.RoleRepository;
import org.example.primera_practica.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {
    
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final MockEndpointRepository mockEndpointRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           ProjectRepository projectRepository,
                           MockEndpointRepository mockEndpointRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.projectRepository = projectRepository;
        this.mockEndpointRepository = mockEndpointRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(ApplicationArguments args) {
        // Create roles if not exist
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleType.ROLE_ADMIN);
                    return roleRepository.save(role);
                });
        
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleType.ROLE_USER);
                    return roleRepository.save(role);
                });
        
        User adminUser = userRepository.findByUsername("admin").orElseGet(() -> {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("admin"));
            user.setEmail("admin@mockapi.com");
            user.setEnabled(true);
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            user.setRoles(roles);
            
            return userRepository.save(user);
        });

        Project usuariosProject = projectRepository.findByName("Usuarios").orElseGet(() -> {
            Project project = new Project();
            project.setName("Usuarios");
            project.setDescription("Proyecto de pruebas para endpoints de usuarios.");
            project.setCreatedBy(adminUser);
            return projectRepository.save(project);
        });

        mockEndpointRepository.findByProjectAndPathAndMethod(
                usuariosProject,
                PathNormalizer.normalizePath("/api/users"),
                HttpMethod.GET
        ).orElseGet(() -> {
            MockEndpoint mockEndpoint = new MockEndpoint();
            mockEndpoint.setName("Usuarios");
            mockEndpoint.setDescription("Listado de usuarios para consumo del frontend.");
            mockEndpoint.setPath(PathNormalizer.normalizePath("/api/users"));
            mockEndpoint.setMethod(HttpMethod.GET);
            mockEndpoint.setHttpStatusCode(200);
            mockEndpoint.setContentType("application/json");
            mockEndpoint.setResponseBody("""
                [
                  { "id": 1, "name": "Ana López", "email": "ana.lopez@example.com" },
                  { "id": 2, "name": "Carlos Pérez", "email": "carlos.perez@example.com" }
                ]
                """.trim());
            mockEndpoint.setExpirationDate(LocalDateTime.now().plusDays(30));
            mockEndpoint.setDelaySeconds(0);
            mockEndpoint.setRequiresJwt(false);
            mockEndpoint.setCreatedBy(adminUser);
            mockEndpoint.setProject(usuariosProject);
            return mockEndpointRepository.save(mockEndpoint);
        });
    }
}
