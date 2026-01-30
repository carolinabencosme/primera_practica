package org.example.primera_practica.service;

import java.time.LocalDateTime;

public interface JwtService {
    String generateToken(String username, LocalDateTime expirationDate);
    String generateTokenForMock(String username, LocalDateTime mockExpirationDate);
    String extractUsername(String token);
    boolean validateToken(String token);
    boolean isTokenExpired(String token);
}
