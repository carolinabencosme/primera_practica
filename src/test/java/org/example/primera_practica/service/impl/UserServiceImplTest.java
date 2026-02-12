package org.example.primera_practica.service.impl;

import org.example.primera_practica.dto.UserDTO;
import org.example.primera_practica.model.Role;
import org.example.primera_practica.model.RoleType;
import org.example.primera_practica.model.User;
import org.example.primera_practica.repository.RoleRepository;
import org.example.primera_practica.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, RoleType.ROLE_USER, "Standard user role");
        adminRole = new Role(2L, RoleType.ROLE_ADMIN, "Admin role");
    }

    @Test
    void createUser_withAdminRole_createsUserWithAdminRole() {
        UserDTO dto = createNewUserDto(Set.of(RoleType.ROLE_ADMIN));
        when(userRepository.findByUsername("adminCandidate")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@demo.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(roleRepository.findByName(RoleType.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        UserDTO created = userService.createUser(dto);

        assertThat(created.getRoles()).containsExactly(RoleType.ROLE_ADMIN);
    }

    @Test
    void createUser_withMultipleRoles_createsUserWithAllRoles() {
        UserDTO dto = createNewUserDto(Set.of(RoleType.ROLE_ADMIN, RoleType.ROLE_USER));
        when(userRepository.findByUsername("adminCandidate")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@demo.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(roleRepository.findByName(RoleType.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return user;
        });

        UserDTO created = userService.createUser(dto);

        assertThat(created.getRoles()).containsExactlyInAnyOrder(RoleType.ROLE_ADMIN, RoleType.ROLE_USER);
    }

    @Test
    void createUser_withoutRoles_assignsDefaultUserRole() {
        UserDTO dto = createNewUserDto(null);
        when(userRepository.findByUsername("adminCandidate")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@demo.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return user;
        });

        UserDTO created = userService.createUser(dto);

        assertThat(created.getRoles()).containsExactly(RoleType.ROLE_USER);
    }

    @Test
    void createUser_withUnknownRole_throwsValidationError() {
        UserDTO dto = createNewUserDto(Set.of(RoleType.ROLE_ADMIN));
        when(userRepository.findByUsername("adminCandidate")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@demo.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleType.ROLE_ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found: ROLE_ADMIN");
    }

    @Test
    void updateUser_withRoles_replacesExistingRoleSet() {
        User persistedUser = new User();
        persistedUser.setId(20L);
        persistedUser.setUsername("existing");
        persistedUser.setEmail("existing@demo.com");
        persistedUser.getRoles().add(userRole);

        UserDTO updateDto = new UserDTO();
        updateDto.setRoles(Set.of(RoleType.ROLE_ADMIN));

        when(userRepository.findById(20L)).thenReturn(Optional.of(persistedUser));
        when(roleRepository.findByName(RoleType.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updated = userService.updateUser(20L, updateDto);

        assertThat(updated.getRoles()).containsExactly(RoleType.ROLE_ADMIN);
    }

    @Test
    void updateUser_withUnknownRole_throwsValidationError() {
        User persistedUser = new User();
        persistedUser.setId(21L);
        persistedUser.setUsername("existing");
        persistedUser.setEmail("existing@demo.com");

        UserDTO updateDto = new UserDTO();
        updateDto.setRoles(Set.of(RoleType.ROLE_ADMIN));

        when(userRepository.findById(21L)).thenReturn(Optional.of(persistedUser));
        when(roleRepository.findByName(RoleType.ROLE_ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(21L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found: ROLE_ADMIN");
    }

    private UserDTO createNewUserDto(Set<RoleType> roles) {
        UserDTO dto = new UserDTO();
        dto.setUsername("adminCandidate");
        dto.setEmail("admin@demo.com");
        dto.setPassword("pass123");
        dto.setConfirmPassword("pass123");
        dto.setRoles(roles);
        return dto;
    }
}
