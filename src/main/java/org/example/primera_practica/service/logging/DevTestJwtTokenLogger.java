package org.example.primera_practica.service.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DevTestJwtTokenLogger implements JwtTokenLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevTestJwtTokenLogger.class);

    @Override
    public void logGeneratedToken(String username, String token) {
        LOGGER.info("JWT generated for user {}: Bearer {}", username, token);
    }
}
