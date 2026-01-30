# Mockup API Server

A powerful Spring Boot-based application for creating, managing, and executing mock API endpoints.

## 📚 Documentation

Comprehensive documentation is available in the [doc/](doc/) directory:

| Document | Description |
|----------|-------------|
| **[README.md](doc/README.md)** | Project overview, features, installation, and quick start guide |
| **[ARCHITECTURE.md](doc/ARCHITECTURE.md)** | System architecture, design patterns, and technical decisions |
| **[DATABASE.md](doc/DATABASE.md)** | Database schema, entities, relationships, and queries |
| **[API.md](doc/API.md)** | REST API reference with request/response examples |
| **[USER_GUIDE.md](doc/USER_GUIDE.md)** | Step-by-step user guide with FAQ and troubleshooting |
| **[SECURITY.md](doc/SECURITY.md)** | Security implementation, JWT, and best practices |
| **[TESTING.md](doc/TESTING.md)** | Testing strategy, test types, and guidelines |
| **[DEPLOYMENT.md](doc/DEPLOYMENT.md)** | Production deployment guide and configuration |
| **[CHANGELOG.md](doc/CHANGELOG.md)** | Version history and release notes |

## 🚀 Quick Start

```bash
# Clone repository
git clone <repository-url>
cd primera_practica

# Run application
./gradlew bootRun

# Access at http://localhost:8080
# Login: admin / admin123
```

## 🔑 Key Features

- **Mock API Management** - Create and manage mock endpoints with custom responses
- **JWT Authentication** - Optional JWT protection for mock endpoints
- **Response Configuration** - Custom status codes, headers, delays, and expiration
- **User Management** - Role-based access control (Admin/User)
- **Web Interface** - Intuitive UI for CRUD operations
- **Dynamic Execution** - REST API for executing mock endpoints

## 📖 Documentation Guide

**New to the project?** Start with [doc/README.md](doc/README.md)

**Want to understand the architecture?** Read [doc/ARCHITECTURE.md](doc/ARCHITECTURE.md)

**Need to deploy?** Follow [doc/DEPLOYMENT.md](doc/DEPLOYMENT.md)

**Looking for API reference?** Check [doc/API.md](doc/API.md)

**End user?** See [doc/USER_GUIDE.md](doc/USER_GUIDE.md)

## 🛠️ Technology Stack

- Spring Boot 4.0.2
- Java 25
- Spring Security 6
- JWT Authentication
- JPA/Hibernate
- H2 Database
- Thymeleaf

## 📄 License

[Specify your license here]
