package org.example.primera_practica.service.logging;

public interface JwtTokenLogger {
    void logGeneratedToken(String username, String token);
}
