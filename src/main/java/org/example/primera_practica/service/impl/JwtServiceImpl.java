package org.example.primera_practica.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.primera_practica.service.JwtService;
import org.example.primera_practica.service.logging.JwtTokenLogger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final ObjectProvider<JwtTokenLogger> jwtTokenLoggerProvider;

    public JwtServiceImpl(ObjectProvider<JwtTokenLogger> jwtTokenLoggerProvider) {
        this.jwtTokenLoggerProvider = jwtTokenLoggerProvider;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateToken(String username, LocalDateTime expirationDate) {
        Date expiry = Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();

        JwtTokenLogger logger = jwtTokenLoggerProvider.getIfAvailable();
        if (logger != null) {
            logger.logGeneratedToken(username, token);
        }

        return token;
    }

    @Override
    public String generateTokenForMock(String username, LocalDateTime mockExpirationDate) {
        return generateToken(username, mockExpirationDate);
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
