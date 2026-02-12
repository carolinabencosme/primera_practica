package org.example.primera_practica.service.impl;

import org.example.primera_practica.dto.UserDTO;
import org.example.primera_practica.exception.DuplicateResourceException;
import org.example.primera_practica.exception.ResourceNotFoundException;
import org.example.primera_practica.model.Role;
import org.example.primera_practica.model.RoleType;
import org.example.primera_practica.model.User;
import org.example.primera_practica.repository.RoleRepository;
import org.example.primera_practica.repository.UserRepository;
import org.example.primera_practica.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userDTO.getConfirmPassword() != null
                && !userDTO.getConfirmPassword().trim().isEmpty()
                && !userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + userDTO.getUsername());
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists: " + userDTO.getEmail());
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEnabled(true);
        user.setRoles(resolveRolesForCreate(userDTO.getRoles()));

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
                throw new DuplicateResourceException("Username already exists: " + userDTO.getUsername());
            }
            user.setUsername(userDTO.getUsername());
        }

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                throw new DuplicateResourceException("Email already exists: " + userDTO.getEmail());
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getRoles() != null) {
            user.setRoles(resolveRoles(userDTO.getRoles()));
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        user.getRoles().add(role);
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private Set<Role> resolveRolesForCreate(Set<RoleType> requestedRoles) {
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            return resolveRoles(Set.of(RoleType.ROLE_USER));
        }
        return resolveRoles(requestedRoles);
    }

    private Set<Role> resolveRoles(Set<RoleType> requestedRoles) {
        Set<RoleType> sanitizedRoles = requestedRoles.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (sanitizedRoles.isEmpty()) {
            throw new IllegalArgumentException("At least one valid role is required");
        }

        Set<Role> roles = sanitizedRoles.stream()
                .map(roleType -> roleRepository.findByName(roleType)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleType)))
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("At least one valid role is required");
        }

        return roles;
    }
}
