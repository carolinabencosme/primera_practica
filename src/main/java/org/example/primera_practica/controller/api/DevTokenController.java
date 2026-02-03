package org.example.primera_practica.controller.api;

import org.example.primera_practica.repository.UserRepository;
import org.example.primera_practica.service.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Profile({"dev", "test"})
@RestController
@RequestMapping("/api/dev")
public class DevTokenController {

    private static final String DEFAULT_USERNAME = "admin";

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public DevTokenController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getDevToken() {
        return buildDevTokenResponse();
    }

    @GetMapping("/jwt")
    public ResponseEntity<Map<String, String>> getDevJwt() {
        return buildDevTokenResponse();
    }

    private ResponseEntity<Map<String, String>> buildDevTokenResponse() {
        return userRepository.findByUsername(DEFAULT_USERNAME)
            .map(user -> {
                String username = user.getUsername();
                String token = jwtService.generateToken(username, LocalDateTime.now().plusDays(1));
                return ResponseEntity.ok(Map.of("token", token, "subject", username));
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error",
                    "User '" + DEFAULT_USERNAME + "' not found. Ensure seed data exists before requesting a dev token."
                )));
    }
}
