package org.example.primera_practica.util;

import org.example.primera_practica.model.Role;
import org.example.primera_practica.model.RoleType;
import org.example.primera_practica.model.User;
import org.example.primera_practica.repository.RoleRepository;
import org.example.primera_practica.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
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
        
        // Create admin user if not exist
        if (!userRepository.findByUsername("admin").isPresent()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@mockapi.com");
            adminUser.setEnabled(true);
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            adminUser.setRoles(roles);
            
            userRepository.save(adminUser);
        }
    }
}
