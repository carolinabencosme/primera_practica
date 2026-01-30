# Changelog

All notable changes to the Mockup API Server project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned Features
- Public JWT authentication API endpoint
- Request logging and history
- Response templating with variables
- Query parameter support in mock endpoints
- Conditional responses based on request data
- API documentation with Swagger/OpenAPI
- Import/Export mock configurations
- Mock endpoint versioning
- WebSocket support
- GraphQL endpoint mocking
- Rate limiting per project
- Analytics dashboard
- Multi-language support for UI
- Dark mode UI theme

---

## [1.0.0] - 2024-01-15

### Added

#### Core Features
- **Mock API Project Management**
  - Create, read, update, delete (CRUD) operations for projects
  - Unique project naming with URL-safe validation
  - Project descriptions and metadata
  - User ownership and tracking
  - Automatic timestamp tracking

- **Mock Endpoint Configuration**
  - Support for all HTTP methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
  - Configurable HTTP status codes (200, 201, 404, 500, etc.)
  - Custom response bodies (JSON, XML, plain text)
  - Content-Type header configuration
  - Custom HTTP response headers
  - Response delay simulation (configurable seconds)
  - Expiration date support for temporary endpoints
  - Optional JWT authentication per endpoint

- **Dynamic Mock Execution**
  - RESTful API endpoint: `/api/mock/{projectName}/{path}`
  - Path-based routing with multi-segment support
  - HTTP method matching
  - Response delay application
  - Expiration date validation (410 GONE for expired)
  - JWT token validation for protected endpoints
  - Custom header injection in responses
  - 404 handling for non-existent mocks

#### Security
- **Authentication**
  - Form-based login for web UI
  - JWT token authentication for API endpoints
  - BCrypt password encryption (strength 10)
  - Session management with Spring Security
  - UserDetailsService implementation

- **Authorization**
  - Role-based access control (RBAC)
  - Two roles: ROLE_ADMIN and ROLE_USER
  - Admin-only user management
  - User-scoped project access
  - URL-based authorization rules

- **JWT Implementation**
  - JJWT library (0.12.6)
  - HMAC-SHA256 signature
  - 24-hour token expiration
  - Username extraction from claims
  - Token validation filter

- **Security Configuration**
  - CSRF protection (disabled for API endpoints)
  - Frame options for H2 console
  - Password encoder bean
  - Security filter chain

#### User Management
- **User CRUD Operations** (Admin only)
  - Create users with username, password, email
  - Update user details
  - Enable/disable user accounts
  - Delete users
  - Role assignment

- **Default Admin User**
  - Username: `admin`
  - Password: `admin`
  - Roles: ROLE_ADMIN, ROLE_USER
  - Auto-initialized on startup via DataInitializer

#### Database
- **H2 Database**
  - In-memory database for development
  - H2 console at `/h2-console`
  - JDBC URL: `jdbc:h2:mem:mockdb`
  - Automatic schema generation (ddl-auto=update)

- **JPA Entities**
  - User (username, password, email, enabled, roles)
  - Role (name, description)
  - Project (name, description, createdBy, createdAt)
  - MockEndpoint (path, method, statusCode, responseBody, headers, etc.)
  - MockHeader (headerKey, headerValue)

- **Relationships**
  - User ↔ Role (Many-to-Many)
  - User → Project (One-to-Many)
  - User → MockEndpoint (One-to-Many)
  - Project → MockEndpoint (One-to-Many, cascade delete)
  - MockEndpoint → MockHeader (One-to-Many, cascade delete)

#### Web UI
- **Thymeleaf Templates**
  - Home page with navigation
  - Login page
  - Project list, create, edit, delete views
  - Mock endpoint list, create, edit, delete views
  - User management (admin only)
  
- **Features**
  - Responsive layout
  - Form validation
  - Error messages
  - Success notifications
  - CSRF token integration

#### Configuration
- **Spring Boot 4.0.2**
  - Auto-configuration
  - Embedded Tomcat server
  - DevTools for hot reload
  - Actuator endpoints (optional)

- **Application Properties**
  - Database configuration
  - JWT secret and expiration
  - Server port (8080)
  - Logging levels
  - H2 console enabled

#### Build & Dependencies
- **Gradle 8.x**
  - Java 25 target
  - Spring Boot Gradle Plugin
  - Dependency management

- **Key Dependencies**
  - Spring Boot Starter Web
  - Spring Boot Starter Data JPA
  - Spring Boot Starter Security
  - Spring Boot Starter Thymeleaf
  - Spring Boot Starter Validation
  - H2 Database
  - JJWT (JWT library)
  - Lombok
  - Spring Boot DevTools

#### Testing
- **JUnit 5**
  - Basic context load test
  - Spring Boot Test support
  - Mockito for mocking
  - Test infrastructure ready

#### Documentation
- **Comprehensive Docs in `/doc`**
  - README.md - Project overview and quick start
  - ARCHITECTURE.md - System design and patterns
  - DATABASE.md - Schema and entity documentation
  - API.md - REST API reference
  - USER_GUIDE.md - End-user instructions
  - SECURITY.md - Security implementation
  - TESTING.md - Testing strategy
  - DEPLOYMENT.md - Production deployment guide
  - CHANGELOG.md - Version history (this file)

### Technical Details

#### Architecture
- Layered architecture (Presentation → Service → Repository → Database)
- MVC pattern for web controllers
- Repository pattern for data access
- DTO pattern for API contracts
- Service layer abstraction with interfaces
- Filter-based JWT authentication

#### Design Patterns Used
- Model-View-Controller (MVC)
- Repository Pattern
- Service Layer Pattern
- Data Transfer Object (DTO)
- Dependency Injection
- Filter Pattern (Security)
- Builder Pattern (Lombok)

#### Enums
- `RoleType`: ROLE_ADMIN, ROLE_USER
- `HttpMethod`: GET, POST, PUT, PATCH, DELETE, OPTIONS

#### Exception Handling
- `ResourceNotFoundException` - Entity not found
- `DuplicateResourceException` - Unique constraint violation
- Global exception handling (future enhancement)

#### Default Configuration Values
- HTTP Status Code: 200
- Content-Type: application/json
- Delay Seconds: 0
- Requires JWT: false
- User Enabled: true

### Known Limitations
- No public JWT login API endpoint
- No request logging/history
- No pagination on list endpoints
- No query parameter support in mocks
- Static responses only (no dynamic data)
- H2 in-memory database (data lost on restart)
- JWT secret hardcoded in properties (should use env var)
- Limited test coverage (~5%)
- No API documentation (Swagger/OpenAPI)
- No rate limiting

### Security Notes
- Default admin password should be changed immediately
- JWT secret should be changed in production
- H2 console should be disabled in production
- HTTPS should be enabled in production
- Environment variables should be used for secrets

---

## Version History

### [1.0.0] - 2024-01-15
- Initial release
- Core mock API functionality
- User management
- JWT authentication
- Spring Security integration
- Web UI with Thymeleaf
- H2 database
- Comprehensive documentation

---

## Upgrade Guide

### From Development to Production

1. **Database**
   - Switch from H2 to PostgreSQL/MySQL
   - Set `spring.jpa.hibernate.ddl-auto=validate`
   - Implement Flyway migrations

2. **Security**
   - Change default admin password
   - Use environment variables for `jwt.secret`
   - Enable HTTPS/TLS
   - Disable H2 console

3. **Configuration**
   - Create `application-prod.properties`
   - Set production logging levels
   - Configure connection pooling
   - Enable actuator endpoints

4. **Deployment**
   - Build optimized JAR: `./gradlew bootJar`
   - Set up systemd service or Docker
   - Configure reverse proxy (Nginx)
   - Set up monitoring and backups

---

## Contributing

When making changes, please:
1. Update this CHANGELOG.md
2. Follow [Keep a Changelog](https://keepachangelog.com/) format
3. Use semantic versioning for releases
4. Document breaking changes clearly

### Change Categories

- **Added**: New features
- **Changed**: Changes to existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security improvements

---

## Support

For questions, issues, or contributions:
- Review documentation in `/doc`
- Check GitHub Issues
- Contact development team

---

## License

[Specify your license here]

---

## Authors

- [Your Name/Team]
- Contributors: [List contributors]

---

## Acknowledgments

- Spring Boot team
- Spring Security team
- JJWT library maintainers
- H2 Database team
- Open source community
