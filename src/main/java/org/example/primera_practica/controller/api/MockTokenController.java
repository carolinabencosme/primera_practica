package org.example.primera_practica.controller.api;

import org.example.primera_practica.dto.MockEndpointDTO;
import org.example.primera_practica.exception.ResourceNotFoundException;
import org.example.primera_practica.service.JwtService;
import org.example.primera_practica.service.MockEndpointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/mocks")
public class MockTokenController {

    private final MockEndpointService mockEndpointService;
    private final JwtService jwtService;

    public MockTokenController(MockEndpointService mockEndpointService, JwtService jwtService) {
        this.mockEndpointService = mockEndpointService;
        this.jwtService = jwtService;
    }

    @GetMapping("/{id}/jwt")
    public ResponseEntity<Map<String, String>> getMockJwt(
        @PathVariable("id") Long id,
        Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Authentication required"));
        }

        MockEndpointDTO mockEndpoint;
        try {
            mockEndpoint = mockEndpointService.getMockEndpointById(id);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
        }

        LocalDateTime expirationDate = mockEndpoint.getExpirationDate();
        if (expirationDate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Mock endpoint expiration date is missing"));
        }

        if (!expirationDate.isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Mock endpoint has already expired"));
        }

        String token = jwtService.generateTokenForMock(authentication.getName(), expirationDate);

        return ResponseEntity.ok(Map.of(
            "token", token,
            "bearer", "Bearer " + token
        ));
    }
}
