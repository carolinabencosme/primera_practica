# Security Documentation

This document describes the security implementation, best practices, and recommendations for the Mockup API Server.

## Table of Contents

- [Overview](#overview)
- [Security Architecture](#security-architecture)
- [Authentication](#authentication)
- [Authorization](#authorization)
- [JWT Implementation](#jwt-implementation)
- [Password Security](#password-security)
- [CSRF Protection](#csrf-protection)
- [Security Configuration](#security-configuration)
- [Best Practices](#best-practices)
- [Production Recommendations](#production-recommendations)
- [Security Checklist](#security-checklist)

## Overview

The Mockup API Server implements multiple layers of security using Spring Security 6, JWT authentication, BCrypt password encryption, and role-based access control.

### Security Features

- ✅ Form-based authentication for web UI
- ✅ JWT token authentication for API endpoints
- ✅ BCrypt password encryption (strength 10)
- ✅ Role-based access control (RBAC)
- ✅ CSRF protection for web forms
- ✅ Session management
- ✅ Secure password storage
- ✅ JWT signature validation
- ✅ Token expiration handling

---

## Security Architecture

### Security Layers

```
┌──────────────────────────────────────────────┐
│          Client Request                       │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│     1. Spring Security Filter Chain           │
│        - CSRF Filter                          │
│        - Session Management Filter            │
│        - Authentication Filter                │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│     2. JWT Authentication Filter              │
│        - Extract JWT from header              │
│        - Validate signature                   │
│        - Check expiration                     │
│        - Set SecurityContext                  │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│     3. Authorization                          │
│        - Check user roles                     │
│        - Verify permissions                   │
│        - Allow/Deny access                    │
└──────────────┬───────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│     4. Controller/Business Logic              │
└──────────────────────────────────────────────┘
```

---

## Authentication

The application supports two authentication methods:

### 1. Form-Based Authentication (Web UI)

**Login Flow:**
1. User navigates to protected resource
2. Redirected to `/login` if not authenticated
3. User submits username/password form
4. Spring Security validates credentials
5. `UserDetailsServiceImpl` loads user from database
6. Password compared using BCrypt
7. Session created with `JSESSIONID` cookie
8. User redirected to originally requested page

**Configuration:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        );
    return http.build();
}
```

**Session Management:**
- Session timeout: Default (30 minutes)
- Concurrent sessions: Allowed
- Session fixation: Protection enabled

---

### 2. JWT Authentication (API)

**JWT Flow:**
1. Client includes JWT in `Authorization` header
2. `JwtAuthenticationFilter` intercepts request
3. Token extracted from header: `Bearer <token>`
4. Token validated (signature, expiration)
5. Username extracted from token claims
6. User loaded from database
7. `SecurityContext` populated with authentication
8. Request proceeds to controller

**Header Format:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Filter Implementation:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            
            if (username != null && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {
                UserDetails userDetails = userDetailsService
                    .loadUserByUsername(username);
                
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## Authorization

### Role-Based Access Control (RBAC)

The application uses two roles:

#### ROLE_ADMIN
- Full system access
- User management (create, edit, delete users)
- Access to all projects and mock endpoints
- View and modify all system data

#### ROLE_USER
- Standard user access
- Create and manage own projects
- Create and manage own mock endpoints
- Cannot manage other users

### URL Authorization Rules

```java
http.authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers("/", "/login", "/logout").permitAll()
    .requestMatchers("/h2-console/**").permitAll()
    .requestMatchers("/api/mock/**").permitAll()
    
    // Admin-only endpoints
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/users/**").hasRole("ADMIN")
    
    // Authenticated endpoints
    .requestMatchers("/projects/**").authenticated()
    .requestMatchers("/mocks/**").authenticated()
    
    // Default
    .anyRequest().authenticated()
);
```

### Method-Level Security

Use `@PreAuthorize` for fine-grained control:

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long userId) {
    // Only admins can execute this
}

@PreAuthorize("hasRole('USER')")
public void createProject(ProjectDTO project) {
    // Any authenticated user
}
```

---

## JWT Implementation

### Token Structure

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload (Claims):**
```json
{
  "sub": "admin",
  "iat": 1705320000,
  "exp": 1705406400
}
```

**Signature:**
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret_key
)
```

### JWT Service Implementation

```java
@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) 
            && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### JWT Configuration

**application.properties:**
```properties
# JWT Secret Key (CHANGE IN PRODUCTION!)
jwt.secret=mockup_api_server_secret_key_2026_cambiar_en_produccion_por_seguridad

# JWT Expiration (24 hours in milliseconds)
jwt.expiration=86400000
```

**Token Lifetime:** 24 hours (86,400,000 ms)

---

## Password Security

### BCrypt Password Encoding

**Configuration:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Encoding Strength:** 10 rounds (default)

**Example:**
- Plain text: `admin`
- BCrypt hash: `$2a$10$XVPGj7kL3...` (60 characters)

### Password Storage

**Never store plain text passwords!**

**User Creation Flow:**
```java
public User createUser(String username, String password) {
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password)); // Encoded!
    return userRepository.save(user);
}
```

### Password Validation

**Login Flow:**
```java
public boolean authenticate(String username, String password) {
    User user = userRepository.findByUsername(username);
    return passwordEncoder.matches(password, user.getPassword());
}
```

**BCrypt Benefits:**
- Salted (unique hash per password)
- Slow algorithm (prevents brute force)
- Configurable work factor
- Industry standard

---

## CSRF Protection

### CSRF (Cross-Site Request Forgery)

**Protection Enabled for:**
- All POST/PUT/DELETE/PATCH requests
- Web forms (Thymeleaf includes token automatically)

**Protection Disabled for:**
- `/api/**` endpoints (stateless JWT authentication)
- `/h2-console/**` (development only)

**Configuration:**
```java
http.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/**", "/h2-console/**")
);
```

### Using CSRF Tokens in Forms

**Thymeleaf (automatic):**
```html
<form th:action="@{/projects}" method="post">
    <!-- CSRF token added automatically -->
    <input type="text" name="name" />
    <button type="submit">Submit</button>
</form>
```

**Manual (if needed):**
```html
<input type="hidden" name="${_csrf.parameterName}" 
       value="${_csrf.token}" />
```

---

## Security Configuration

### Complete Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            // CSRF
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/mock/**").permitAll()
                .anyRequest().authenticated()
            )
            
            // Form login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            
            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            
            // Frame options (for H2 console)
            .headers(headers -> headers
                .frameOptions(FrameOptionsConfig::sameOrigin)
            )
            
            // JWT filter
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Best Practices

### 1. Password Management

**DO:**
- ✅ Enforce minimum password length (8+ characters)
- ✅ Use BCrypt or Argon2 for encoding
- ✅ Never log passwords
- ✅ Never display passwords in UI
- ✅ Change default passwords immediately
- ✅ Implement password change functionality

**DON'T:**
- ❌ Store plain text passwords
- ❌ Use weak hashing (MD5, SHA1)
- ❌ Hard-code passwords in code
- ❌ Include passwords in URLs
- ❌ Transmit passwords without HTTPS

---

### 2. JWT Security

**DO:**
- ✅ Use strong secret keys (256+ bits)
- ✅ Store secret in environment variables
- ✅ Set reasonable expiration times
- ✅ Validate token signature
- ✅ Check token expiration
- ✅ Use HTTPS in production

**DON'T:**
- ❌ Store secrets in code/properties file
- ❌ Use weak secrets (e.g., "secret123")
- ❌ Set excessively long expiration
- ❌ Trust token payload without validation
- ❌ Store sensitive data in JWT payload

---

### 3. Session Management

**DO:**
- ✅ Use secure session cookies
- ✅ Set appropriate session timeout
- ✅ Invalidate sessions on logout
- ✅ Prevent session fixation
- ✅ Use HTTPS for session cookies

**DON'T:**
- ❌ Allow unlimited concurrent sessions
- ❌ Store sensitive data in sessions
- ❌ Use predictable session IDs

---

### 4. Access Control

**DO:**
- ✅ Apply principle of least privilege
- ✅ Use role-based access control
- ✅ Validate permissions on every request
- ✅ Log access attempts
- ✅ Implement audit trails

**DON'T:**
- ❌ Rely on client-side validation only
- ❌ Expose admin functionality to users
- ❌ Trust user input
- ❌ Skip authorization checks

---

## Production Recommendations

### 1. Environment Configuration

**Use Environment Variables:**
```bash
# DO NOT hard-code in application.properties
export JWT_SECRET="your-super-secure-random-256-bit-key"
export DB_PASSWORD="strong-database-password"
export ADMIN_PASSWORD="strong-admin-password"
```

**application-prod.properties:**
```properties
# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000  # 1 hour (shorter in production)

# Database
spring.datasource.password=${DB_PASSWORD}

# Security
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
```

---

### 2. HTTPS/TLS

**Enable HTTPS:**
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=${SSL_PASSWORD}
server.port=8443
```

**Force HTTPS:**
```java
http.requiresChannel(channel -> channel
    .anyRequest().requiresSecure()
);
```

---

### 3. Disable Development Features

**Disable H2 Console:**
```properties
spring.h2.console.enabled=false
```

**Disable Debug Logging:**
```properties
logging.level.org.springframework.security=WARN
logging.level.org.example=INFO
```

**Remove DevTools:**
```gradle
// Comment out in build.gradle
// developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

---

### 4. Security Headers

**Add Security Headers:**
```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'")
    )
    .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
    .contentTypeOptions(Customizer.withDefaults())
    .frameOptions(frame -> frame.deny())
);
```

**Headers Added:**
- `Content-Security-Policy`: Prevent XSS
- `X-Content-Type-Options: nosniff`: Prevent MIME sniffing
- `X-Frame-Options: DENY`: Prevent clickjacking
- `X-XSS-Protection: 1; mode=block`: XSS filter

---

### 5. Rate Limiting

**Implement Rate Limiting:**
```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimiter rateLimiter = 
        RateLimiter.create(100.0); // 100 requests/sec
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) {
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(429); // Too Many Requests
            return;
        }
        chain.doFilter(request, response);
    }
}
```

---

### 6. Input Validation

**Validate All Input:**
```java
@PostMapping("/projects")
public ResponseEntity<?> createProject(@Valid @RequestBody ProjectDTO dto) {
    // @Valid triggers Bean Validation
    return ResponseEntity.ok(projectService.create(dto));
}
```

**DTO Validation:**
```java
public class ProjectDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50)
    private String name;
    
    @Size(max = 500)
    private String description;
}
```

---

### 7. SQL Injection Prevention

**Use JPA/Hibernate:**
```java
// SAFE: Parameterized query
@Query("SELECT p FROM Project p WHERE p.name = :name")
Optional<Project> findByName(@Param("name") String name);
```

**Avoid Raw SQL:**
```java
// UNSAFE: String concatenation
// entityManager.createNativeQuery("SELECT * FROM project WHERE name = '" + name + "'");
```

---

### 8. Audit Logging

**Log Security Events:**
```java
@Component
public class SecurityAuditListener {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditListener.class);
    
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        logger.info("Login success: {}", event.getAuthentication().getName());
    }
    
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        logger.warn("Login failed: {}", event.getAuthentication().getName());
    }
}
```

---

## Security Checklist

### Development

- [ ] Default admin password changed
- [ ] JWT secret key is strong and random
- [ ] Passwords stored with BCrypt
- [ ] CSRF protection enabled for forms
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention (JPA)
- [ ] XSS prevention (output encoding)

### Production

- [ ] HTTPS enabled (TLS 1.2+)
- [ ] JWT secret in environment variable
- [ ] Database password in environment variable
- [ ] H2 console disabled
- [ ] Debug logging disabled
- [ ] DevTools removed
- [ ] Security headers configured
- [ ] Rate limiting implemented
- [ ] Session timeout configured
- [ ] Firewall rules configured
- [ ] Regular security updates applied
- [ ] Audit logging enabled
- [ ] Backup and recovery tested

---

## Security Incident Response

### If Credentials Are Compromised

1. **Immediately change passwords**
2. **Revoke JWT tokens** (implement token blacklist)
3. **Review access logs** for suspicious activity
4. **Notify affected users**
5. **Update security measures**

### If Secret Key Is Exposed

1. **Generate new secret key**
2. **Update environment variables**
3. **Restart application**
4. **Invalidate all existing JWT tokens**
5. **Force re-authentication**

---

## Conclusion

Security is a continuous process. Regularly review and update security configurations, apply patches, and follow best practices. This documentation should be updated as new security features are added or vulnerabilities are discovered.

**Remember**: Security is only as strong as the weakest link. Train your team, automate security testing, and stay informed about emerging threats.
