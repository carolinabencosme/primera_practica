# Mockup API Server

A powerful Spring Boot-based application that enables developers to create, manage, and execute mock API endpoints with configurable responses, delays, and authentication requirements.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [System Requirements](#system-requirements)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Default Credentials](#default-credentials)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Quick Start Guide](#quick-start-guide)

## Features

- **Mock API Project Management**: Create and organize mock endpoints into logical projects
- **Dynamic Mock Execution**: Execute mock requests through REST API with configurable responses
- **Flexible Response Configuration**: 
  - Custom HTTP status codes (200, 404, 500, etc.)
  - JSON, XML, or plain text response bodies
  - Custom response headers
  - Configurable response delays
  - Expiration dates for temporary mocks
- **JWT Authentication**: Optional JWT validation for individual mock endpoints
- **Role-Based Access Control**: Admin and User roles with different permissions
- **User Management**: Admin interface for creating and managing users
- **Web UI**: Intuitive web interface for CRUD operations
- **H2 Database Console**: Built-in database viewer for development
- **Security**: Spring Security with BCrypt password encryption

## Technologies Used

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 4.0.2 |
| Language | Java | 25 |
| Build Tool | Gradle | 8.x |
| Database | H2 Database | Runtime |
| ORM | Spring Data JPA | - |
| Security | Spring Security | 6.x |
| Authentication | JWT (JJWT) | 0.12.6 |
| Template Engine | Thymeleaf | - |
| Validation | Jakarta Bean Validation | - |
| Utilities | Lombok, Spring DevTools | - |
| Testing | JUnit 5, Spring Boot Test | - |

## System Requirements

- **Java**: JDK 25 or higher
- **Gradle**: 8.0 or higher (or use included Gradle wrapper)
- **Memory**: Minimum 512MB RAM
- **Disk Space**: 100MB for application and dependencies
- **OS**: Windows, Linux, or macOS

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd primera_practica
```

### 2. Build the Project

Using Gradle wrapper (recommended):

```bash
# Linux/macOS
./gradlew build

# Windows
gradlew.bat build
```

Using installed Gradle:

```bash
gradle build
```

### 3. Run Tests (Optional)

```bash
./gradlew test
```

## Running the Application

### Development Mode

```bash
./gradlew bootRun
```

### Production Mode

```bash
# Build JAR file
./gradlew bootJar

# Run the JAR
java -jar build/libs/primera_practica-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

### Accessing the Application

- **Main Application**: http://localhost:8080
- **Login Page**: http://localhost:8080/login
- **H2 Console** (Development only): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:mockdb`
  - Username: `sa`
  - Password: (leave blank)

## Default Credentials

The application initializes with a default admin user:

- **Username**: `admin`
- **Password**: `admin`
- **Roles**: ROLE_ADMIN, ROLE_USER

**⚠️ IMPORTANT**: Change the default password immediately after first login, especially in production environments.

## Project Structure

```
primera_practica/
├── src/
│   ├── main/
│   │   ├── java/org/example/primera_practica/
│   │   │   ├── config/              # Security and web configuration
│   │   │   ├── controller/          # REST and MVC controllers
│   │   │   │   ├── api/             # API endpoints
│   │   │   │   └── web/             # Web UI controllers
│   │   │   ├── service/             # Business logic
│   │   │   │   └── impl/            # Service implementations
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── model/               # JPA entities
│   │   │   ├── security/            # Security filters and configs
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── exception/           # Custom exceptions
│   │   │   └── util/                # Utilities and initializers
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf templates
│   │       ├── static/              # CSS, JS, images
│   │       └── application.properties
│   └── test/                        # Test files
├── build.gradle                     # Gradle build configuration
├── settings.gradle
├── doc/                             # Documentation
└── README.md
```

## Documentation

Comprehensive documentation is available in the `doc/` directory:

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and design patterns
- **[DATABASE.md](DATABASE.md)** - Database schema and entity relationships
- **[API.md](API.md)** - REST API endpoints and usage
- **[USER_GUIDE.md](USER_GUIDE.md)** - End-user guide with step-by-step instructions
- **[SECURITY.md](SECURITY.md)** - Security implementation and best practices
- **[TESTING.md](TESTING.md)** - Testing strategy and guidelines
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Production deployment guide
- **[CHANGELOG.md](CHANGELOG.md)** - Version history and changes

## Quick Start Guide

### 1. Start the Application

```bash
./gradlew bootRun
```

### 2. Login

Navigate to http://localhost:8080 and login with:
- Username: `admin`
- Password: `admin`

### 3. Create a Project

1. Click **"Projects"** in the navigation menu
2. Click **"Create New Project"**
3. Fill in:
   - **Name**: `UserAPI`
   - **Description**: `Mock endpoints for user management`
4. Click **"Save"**

### 4. Create a Mock Endpoint

1. From the project details page, click **"Add Mock Endpoint"**
2. Fill in:
   - **Path**: `/users/1`
   - **HTTP Method**: `GET`
   - **Status Code**: `200`
   - **Response Body**:
     ```json
     {
       "id": 1,
       "name": "John Doe",
       "email": "john@example.com"
     }
     ```
3. Click **"Save"**

### 5. Test the Mock Endpoint

Execute the mock endpoint:

```bash
curl http://localhost:8080/api/mock/UserAPI/users/1
```

Response:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

### 6. Create a JWT-Protected Endpoint

1. Create another mock endpoint with **"Requires JWT"** checked
2. Generate a JWT token (admin user can generate tokens via the UI or API)
3. Execute with JWT:

```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
     http://localhost:8080/api/mock/UserAPI/protected/resource
```

## Support and Contributing

For issues, questions, or contributions, please refer to the project repository.

## License

[Specify your license here]
