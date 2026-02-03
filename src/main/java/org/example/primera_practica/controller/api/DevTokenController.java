package org.example.primera_practica.controller.api;

import org.example.primera_practica.service.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Profile({"dev", "test"})
@RestController
@RequestMapping("/api/dev")
public class DevTokenController {

    private final JwtService jwtService;

    public DevTokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/jwt")
    public ResponseEntity<Map<String, String>> getDevJwt(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Authentication required"));
        }

        String username = authentication.getName();
        String token = jwtService.generateToken(username, LocalDateTime.now().plusDays(1));

        return ResponseEntity.ok(Map.of(
            "token", token,
            "bearer", "Bearer " + token
        ));
    }
}
