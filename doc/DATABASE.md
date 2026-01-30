# Database Documentation

This document describes the database schema, entity relationships, and data model of the Mockup API Server.

## Table of Contents

- [Overview](#overview)
- [Entity-Relationship Diagram](#entity-relationship-diagram)
- [Entities](#entities)
- [Relationships](#relationships)
- [Constraints and Indexes](#constraints-and-indexes)
- [Sample Queries](#sample-queries)
- [Database Configuration](#database-configuration)

## Overview

The Mockup API Server uses **H2 Database** with **JPA/Hibernate** for object-relational mapping. The schema consists of 5 main entities that support user management, project organization, and mock endpoint configuration.

### Database Technology

- **Database**: H2 Database Engine
- **Mode**: In-memory (development) or file-based (persistent)
- **ORM**: Hibernate 6.x
- **DDL Strategy**: `update` (automatic schema evolution)
- **JDBC URL**: `jdbc:h2:mem:mockdb`

## Entity-Relationship Diagram

```
┌─────────────────────┐
│       ROLE          │
│ ─────────────────── │
│ PK  id (Long)       │
│     name (String)   │◄──────┐
│     description     │       │
└─────────────────────┘       │
                              │ Many-to-Many
                              │ (user_roles join table)
┌─────────────────────┐       │
│       USER          │       │
│ ─────────────────── │       │
│ PK  id (Long)       │───────┘
│     username *U     │
│     password        │
│     email *U        │
│     enabled         │
│     createdAt       │──┐
└─────────────────────┘  │
           │             │ One-to-Many
           │             │ (createdBy)
           └─────────────┼──────────────┐
                         │              │
                         │              │
┌─────────────────────┐  │              │
│      PROJECT        │  │              │
│ ─────────────────── │  │              │
│ PK  id (Long)       │  │              │
│     name *U         │  │              │
│     description     │  │              │
│ FK  createdBy       │──┘              │
│     createdAt       │                 │
└─────────────────────┘                 │
           │                            │
           │ One-to-Many                │
           │                            │
           ▼                            │
┌─────────────────────┐                 │
│   MOCK_ENDPOINT     │                 │
│ ─────────────────── │                 │
│ PK  id (Long)       │                 │
│     path            │                 │
│     method (enum)   │                 │
│     httpStatusCode  │                 │
│     responseBody    │                 │
│     contentType     │                 │
│     expirationDate  │                 │
│     delaySeconds    │                 │
│     requiresJwt     │                 │
│ FK  project_id      │─────────────────┘
│ FK  createdBy       │──────────────────┘
│     createdAt       │
└─────────────────────┘
           │
           │ One-to-Many
           │ (cascade)
           ▼
┌─────────────────────┐
│    MOCK_HEADER      │
│ ─────────────────── │
│ PK  id (Long)       │
│     headerKey       │
│     headerValue     │
│ FK  mockEndpoint_id │
└─────────────────────┘

Legend:
  PK  = Primary Key
  FK  = Foreign Key
  *U  = Unique Constraint
```

## Entities

### 1. User

Represents system users who can create projects and mock endpoints.

**Table Name**: `user` (or `users` depending on H2 configuration)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `username` | VARCHAR(255) | NOT NULL, UNIQUE | Login username |
| `password` | VARCHAR(255) | NOT NULL | BCrypt-encoded password |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | User email address |
| `enabled` | BOOLEAN | NOT NULL, DEFAULT TRUE | Account active status |
| `created_at` | TIMESTAMP | NOT NULL | Account creation timestamp |

**JPA Entity:**
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Default Data:**
- Username: `admin`
- Password: `admin` (stored as BCrypt hash)
- Email: `admin@mockup.com`
- Roles: `ROLE_ADMIN`, `ROLE_USER`

---

### 2. Role

Defines user roles and permissions.

**Table Name**: `role`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(50) | NOT NULL, UNIQUE | Role name (e.g., ROLE_ADMIN) |
| `description` | VARCHAR(255) | | Role description |

**JPA Entity:**
```java
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;
    
    private String description;
}
```

**Enum: RoleType**
```java
public enum RoleType {
    ROLE_ADMIN,  // Full system access
    ROLE_USER    // Standard user access
}
```

**Default Roles:**
1. `ROLE_ADMIN` - "Administrator role with full access"
2. `ROLE_USER` - "Standard user role"

---

### 3. Project

Logical grouping for mock endpoints.

**Table Name**: `project`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL, UNIQUE | Project name (URL-safe) |
| `description` | TEXT | | Project description |
| `created_by` | BIGINT | FOREIGN KEY → user.id | Creator user ID |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |

**JPA Entity:**
```java
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockEndpoint> mockEndpoints = new ArrayList<>();
}
```

**Business Rules:**
- Project name must be unique (used in URL routing)
- Project name should be URL-safe (no spaces or special characters)
- Deleting a project cascades to all its mock endpoints

---

### 4. MockEndpoint

Defines a mock API endpoint with configurable response.

**Table Name**: `mock_endpoint`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `path` | VARCHAR(500) | NOT NULL | Endpoint path (e.g., /users/1) |
| `method` | VARCHAR(10) | NOT NULL | HTTP method (GET, POST, etc.) |
| `http_status_code` | INTEGER | NOT NULL, DEFAULT 200 | HTTP response code |
| `response_body` | TEXT | | Response content (JSON, XML, etc.) |
| `content_type` | VARCHAR(100) | DEFAULT 'application/json' | Content-Type header |
| `expiration_date` | TIMESTAMP | | Optional expiration date |
| `delay_seconds` | INTEGER | DEFAULT 0 | Response delay in seconds |
| `requires_jwt` | BOOLEAN | DEFAULT FALSE | JWT authentication required |
| `project_id` | BIGINT | NOT NULL, FOREIGN KEY → project.id | Parent project |
| `created_by` | BIGINT | FOREIGN KEY → user.id | Creator user ID |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |

**JPA Entity:**
```java
@Entity
public class MockEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String path;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HttpMethod method;
    
    @Column(name = "http_status_code", nullable = false)
    private Integer httpStatusCode = 200;
    
    @Column(columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "content_type")
    private String contentType = "application/json";
    
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    
    @Column(name = "delay_seconds")
    private Integer delaySeconds = 0;
    
    @Column(name = "requires_jwt")
    private Boolean requiresJwt = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @OneToMany(mappedBy = "mockEndpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockHeader> headers = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Enum: HttpMethod**
```java
public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE, OPTIONS
}
```

**Business Rules:**
- Path must start with `/`
- Combination of (project_id, path, method) should be unique
- Expired endpoints return 410 GONE
- Delay is applied before response (simulates latency)

---

### 5. MockHeader

Custom HTTP response headers for mock endpoints.

**Table Name**: `mock_header`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `header_key` | VARCHAR(100) | NOT NULL | Header name (e.g., X-Custom) |
| `header_value` | VARCHAR(500) | NOT NULL | Header value |
| `mock_endpoint_id` | BIGINT | NOT NULL, FOREIGN KEY → mock_endpoint.id | Parent endpoint |

**JPA Entity:**
```java
@Entity
public class MockHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "header_key", nullable = false)
    private String headerKey;
    
    @Column(name = "header_value", nullable = false, length = 500)
    private String headerValue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_endpoint_id", nullable = false)
    private MockEndpoint mockEndpoint;
}
```

**Common Use Cases:**
- `X-Request-ID`: Unique request identifier
- `X-Rate-Limit-Remaining`: Rate limit info
- `Cache-Control`: Caching directives
- `X-Custom-Data`: Any custom metadata

---

## Relationships

### User ↔ Role (Many-to-Many)

**Join Table**: `user_roles`

| Column | Type | Constraints |
|--------|------|-------------|
| `user_id` | BIGINT | FOREIGN KEY → user.id |
| `role_id` | BIGINT | FOREIGN KEY → role.id |

**Composite Primary Key**: (user_id, role_id)

**Fetch Strategy**: EAGER (roles loaded with user)

---

### User ↔ Project (One-to-Many)

- One user can create many projects
- `Project.createdBy` references `User.id`
- **Cascade**: None (projects remain if user deleted - or configure orphan removal)
- **Fetch Strategy**: LAZY

---

### User ↔ MockEndpoint (One-to-Many)

- One user can create many mock endpoints
- `MockEndpoint.createdBy` references `User.id`
- **Cascade**: None
- **Fetch Strategy**: LAZY

---

### Project ↔ MockEndpoint (One-to-Many)

- One project contains many mock endpoints
- `MockEndpoint.project_id` references `Project.id`
- **Cascade**: ALL with orphan removal (delete project → delete endpoints)
- **Fetch Strategy**: LAZY

---

### MockEndpoint ↔ MockHeader (One-to-Many)

- One mock endpoint can have many headers
- `MockHeader.mock_endpoint_id` references `MockEndpoint.id`
- **Cascade**: ALL with orphan removal
- **Fetch Strategy**: LAZY

---

## Constraints and Indexes

### Primary Keys

All entities use auto-generated surrogate keys:
```sql
id BIGINT AUTO_INCREMENT PRIMARY KEY
```

### Unique Constraints

```sql
-- User entity
ALTER TABLE user ADD CONSTRAINT uk_username UNIQUE (username);
ALTER TABLE user ADD CONSTRAINT uk_email UNIQUE (email);

-- Role entity
ALTER TABLE role ADD CONSTRAINT uk_role_name UNIQUE (name);

-- Project entity
ALTER TABLE project ADD CONSTRAINT uk_project_name UNIQUE (name);
```

### Foreign Keys

```sql
-- Project → User
ALTER TABLE project 
  ADD CONSTRAINT fk_project_user 
  FOREIGN KEY (created_by) REFERENCES user(id);

-- MockEndpoint → Project
ALTER TABLE mock_endpoint 
  ADD CONSTRAINT fk_endpoint_project 
  FOREIGN KEY (project_id) REFERENCES project(id)
  ON DELETE CASCADE;

-- MockEndpoint → User
ALTER TABLE mock_endpoint 
  ADD CONSTRAINT fk_endpoint_user 
  FOREIGN KEY (created_by) REFERENCES user(id);

-- MockHeader → MockEndpoint
ALTER TABLE mock_header 
  ADD CONSTRAINT fk_header_endpoint 
  FOREIGN KEY (mock_endpoint_id) REFERENCES mock_endpoint(id)
  ON DELETE CASCADE;

-- User_Roles join table
ALTER TABLE user_roles
  ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES user(id),
  ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES role(id);
```

### Recommended Indexes

For optimal query performance:

```sql
-- Improve project lookup by name
CREATE INDEX idx_project_name ON project(name);

-- Improve mock endpoint lookup
CREATE INDEX idx_endpoint_project_path ON mock_endpoint(project_id, path, method);

-- Improve user authentication
CREATE INDEX idx_user_username ON user(username);

-- Improve expiration checks
CREATE INDEX idx_endpoint_expiration ON mock_endpoint(expiration_date);
```

---

## Sample Queries

### 1. Find User with Roles

```sql
SELECT u.*, r.name as role_name
FROM user u
JOIN user_roles ur ON u.id = ur.user_id
JOIN role r ON ur.role_id = r.id
WHERE u.username = 'admin';
```

### 2. List All Projects with Creator

```sql
SELECT p.id, p.name, p.description, u.username as created_by, p.created_at
FROM project p
LEFT JOIN user u ON p.created_by = u.id
ORDER BY p.created_at DESC;
```

### 3. Find Mock Endpoint for Execution

```sql
SELECT me.*, p.name as project_name
FROM mock_endpoint me
JOIN project p ON me.project_id = p.id
WHERE p.name = 'UserAPI'
  AND me.path = '/users/1'
  AND me.method = 'GET'
  AND (me.expiration_date IS NULL OR me.expiration_date > CURRENT_TIMESTAMP);
```

### 4. Get All Headers for an Endpoint

```sql
SELECT mh.header_key, mh.header_value
FROM mock_header mh
WHERE mh.mock_endpoint_id = 123;
```

### 5. List Expired Endpoints

```sql
SELECT p.name as project, me.path, me.method, me.expiration_date
FROM mock_endpoint me
JOIN project p ON me.project_id = p.id
WHERE me.expiration_date < CURRENT_TIMESTAMP
ORDER BY me.expiration_date DESC;
```

### 6. Count Endpoints per Project

```sql
SELECT p.name, COUNT(me.id) as endpoint_count
FROM project p
LEFT JOIN mock_endpoint me ON p.id = me.project_id
GROUP BY p.id, p.name
ORDER BY endpoint_count DESC;
```

### 7. Find All Admin Users

```sql
SELECT u.username, u.email
FROM user u
JOIN user_roles ur ON u.id = ur.user_id
JOIN role r ON ur.role_id = r.id
WHERE r.name = 'ROLE_ADMIN';
```

### 8. Search Endpoints by Response Content

```sql
SELECT p.name, me.path, me.method
FROM mock_endpoint me
JOIN project p ON me.project_id = p.id
WHERE me.response_body LIKE '%error%'
  OR me.response_body LIKE '%404%';
```

---

## Database Configuration

### application.properties

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:mockdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console (Development Only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=false
```

### DDL Auto Strategies

| Strategy | Description | Use Case |
|----------|-------------|----------|
| `none` | No action | Production with manual migrations |
| `validate` | Validate schema | Production |
| `update` | Update schema | Development (current) |
| `create` | Create schema on startup | Testing |
| `create-drop` | Create on start, drop on stop | Integration tests |

**Current**: `update` - Automatically evolves schema based on entity changes

---

## Production Considerations

### 1. Switch to Production Database

Replace H2 with PostgreSQL or MySQL:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/mockdb
spring.datasource.username=mockuser
spring.datasource.password=${DB_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 2. Use Flyway/Liquibase

Manage schema migrations with version control:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### 3. Disable DDL Auto

```properties
spring.jpa.hibernate.ddl-auto=validate
```

### 4. Add Connection Pooling

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

---

## Conclusion

The database schema is designed for flexibility and extensibility. The normalized structure supports multi-tenancy (users own projects), role-based access control, and dynamic mock endpoint configuration with minimal redundancy.
