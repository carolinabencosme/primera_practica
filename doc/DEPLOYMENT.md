# Deployment Guide

This document provides comprehensive instructions for deploying the Mockup API Server to production environments.

## Table of Contents

- [Production Requirements](#production-requirements)
- [Environment Variables](#environment-variables)
- [Production Configuration](#production-configuration)
- [Building for Production](#building-for-production)
- [Database Setup](#database-setup)
- [Deployment Methods](#deployment-methods)
- [Monitoring and Maintenance](#monitoring-and-maintenance)
- [Performance Optimization](#performance-optimization)
- [Troubleshooting](#troubleshooting)

## Production Requirements

### System Requirements

**Minimum:**
- **CPU**: 2 cores
- **RAM**: 2 GB
- **Disk**: 10 GB
- **OS**: Linux (Ubuntu 20.04+, CentOS 8+), Windows Server, macOS

**Recommended:**
- **CPU**: 4 cores
- **RAM**: 4 GB
- **Disk**: 20 GB SSD
- **OS**: Linux (Ubuntu 22.04 LTS)

### Software Requirements

| Component | Version | Required |
|-----------|---------|----------|
| Java JDK | 17+ | ✅ Yes |
| Database | PostgreSQL 13+ or MySQL 8+ | ✅ Yes |
| Reverse Proxy | Nginx or Apache | ⚠️ Recommended |
| SSL Certificate | Let's Encrypt or Commercial | ⚠️ Recommended |
| Process Manager | systemd, PM2, or Docker | ⚠️ Recommended |

---

## Environment Variables

### Required Variables

Create a `.env` file or set system environment variables:

```bash
# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Database (PostgreSQL Example)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mockdb
DB_USERNAME=mockuser
DB_PASSWORD=your_secure_database_password

# Security
JWT_SECRET=your_256_bit_random_secret_key_change_this_in_production
JWT_EXPIRATION=3600000  # 1 hour in milliseconds

# Admin User
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_secure_admin_password
ADMIN_EMAIL=admin@yourcompany.com

# Optional: SSL
SSL_ENABLED=true
SSL_KEY_STORE_PATH=/path/to/keystore.p12
SSL_KEY_STORE_PASSWORD=your_keystore_password

# Optional: SMTP (for future email features)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_email_password
```

### Generating Secrets

**JWT Secret (256-bit):**
```bash
# Linux/macOS
openssl rand -base64 32

# Or using Java
java -cp /path/to/app.jar org.springframework.boot.loader.JarLauncher \
  --spring.main.lazy-initialization=true \
  --spring.main.web-application-type=none \
  org.example.GenerateSecret
```

**Strong Passwords:**
```bash
openssl rand -base64 24
```

---

## Production Configuration

### 1. Create Production Properties

**src/main/resources/application-prod.properties:**

```properties
# ===================================================================
# PRODUCTION CONFIGURATION
# ===================================================================

# ===================================================================
# Server Configuration
# ===================================================================
server.port=${SERVER_PORT:8080}
server.error.include-message=never
server.error.include-stacktrace=never
server.error.include-exception=false

# SSL/TLS Configuration
server.ssl.enabled=${SSL_ENABLED:false}
server.ssl.key-store=${SSL_KEY_STORE_PATH}
server.ssl.key-store-password=${SSL_KEY_STORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat

# ===================================================================
# Database Configuration
# ===================================================================
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# ===================================================================
# Security Configuration
# ===================================================================
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:3600000}

# ===================================================================
# Logging Configuration
# ===================================================================
logging.level.root=WARN
logging.level.org.example.primera_practica=INFO
logging.level.org.springframework.web=WARN
logging.level.org.springframework.security=WARN
logging.level.org.hibernate=WARN

# Log file
logging.file.name=/var/log/mockup-api-server/application.log
logging.file.max-size=10MB
logging.file.max-history=30
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# ===================================================================
# Actuator (Monitoring)
# ===================================================================
management.endpoints.web.base-path=/actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true

# ===================================================================
# Disable Development Features
# ===================================================================
spring.h2.console.enabled=false
spring.devtools.restart.enabled=false
spring.devtools.livereload.enabled=false

# ===================================================================
# Compression
# ===================================================================
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
```

---

### 2. Database Migration (Flyway)

**Add Flyway to build.gradle:**

```gradle
dependencies {
    implementation 'org.flywaydb:flyway-core'
    runtimeOnly 'org.flywaydb:flyway-database-postgresql'
}
```

**Create Migration Scripts:**

**src/main/resources/db/migration/V1__initial_schema.sql:**

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- User-Roles join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Projects table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Mock Endpoints table
CREATE TABLE mock_endpoints (
    id BIGSERIAL PRIMARY KEY,
    path VARCHAR(500) NOT NULL,
    method VARCHAR(10) NOT NULL,
    http_status_code INTEGER NOT NULL DEFAULT 200,
    response_body TEXT,
    content_type VARCHAR(100) DEFAULT 'application/json',
    expiration_date TIMESTAMP,
    delay_seconds INTEGER DEFAULT 0,
    requires_jwt BOOLEAN DEFAULT false,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Mock Headers table
CREATE TABLE mock_headers (
    id BIGSERIAL PRIMARY KEY,
    header_key VARCHAR(100) NOT NULL,
    header_value VARCHAR(500) NOT NULL,
    mock_endpoint_id BIGINT NOT NULL REFERENCES mock_endpoints(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_mock_endpoints_project_path ON mock_endpoints(project_id, path, method);
CREATE INDEX idx_users_username ON users(username);
```

**src/main/resources/db/migration/V2__insert_default_roles.sql:**

```sql
-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator role with full access'),
('ROLE_USER', 'Standard user role');
```

---

## Building for Production

### 1. Build Executable JAR

```bash
# Clean and build
./gradlew clean build

# Skip tests (if already tested)
./gradlew clean build -x test

# Build optimized for production
./gradlew clean bootJar -Pprod
```

**Output:** `build/libs/primera_practica-0.0.1-SNAPSHOT.jar`

---

### 2. Optimize JAR Size

**build.gradle:**

```gradle
bootJar {
    archiveFileName = 'mockup-api-server.jar'
    
    // Exclude unnecessary files
    exclude 'static/**/*.map'
    exclude '**/*.md'
}
```

---

### 3. Create Runnable Script

**run.sh:**

```bash
#!/bin/bash

# Load environment variables
export $(cat .env | xargs)

# Set production profile
export SPRING_PROFILES_ACTIVE=prod

# Run application
java -jar \
  -Xms512m \
  -Xmx2048m \
  -Dspring.profiles.active=prod \
  -Dserver.port=8080 \
  mockup-api-server.jar
```

```bash
chmod +x run.sh
```

---

## Database Setup

### Nota sobre H2

- Con `jdbc:h2:mem:...` los datos se pierden en cada reinicio.
- En plataformas con filesystem efímero, un `jdbc:h2:file:...` tampoco garantiza persistencia.

### PostgreSQL Setup

**1. Install PostgreSQL:**

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# CentOS/RHEL
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
sudo systemctl start postgresql
```

**2. Create Database and User:**

```bash
sudo -u postgres psql
```

```sql
-- Create user
CREATE USER mockuser WITH PASSWORD 'your_secure_password';

-- Create database
CREATE DATABASE mockdb OWNER mockuser;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mockdb TO mockuser;

-- Exit
\q
```

**3. Configure PostgreSQL Access:**

Edit `/etc/postgresql/13/main/pg_hba.conf`:

```
# Allow local connections with password
local   all             all                                     md5
host    all             all             127.0.0.1/32            md5
host    all             all             ::1/128                 md5
```

Restart PostgreSQL:

```bash
sudo systemctl restart postgresql
```

---

### MySQL Setup

**1. Install MySQL:**

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server
sudo systemctl start mysqld
```

**2. Secure Installation:**

```bash
sudo mysql_secure_installation
```

**3. Create Database and User:**

```bash
sudo mysql -u root -p
```

```sql
-- Create database
CREATE DATABASE mockdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'mockuser'@'localhost' IDENTIFIED BY 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON mockdb.* TO 'mockuser'@'localhost';
FLUSH PRIVILEGES;

-- Exit
EXIT;
```

**4. Update application-prod.properties:**

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST}:3306/${DB_NAME}?useSSL=true&serverTimezone=UTC
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

---

## Deployment Methods

### Method 1: Systemd Service (Linux)

**1. Create Service File:**

**/etc/systemd/system/mockup-api-server.service:**

```ini
[Unit]
Description=Mockup API Server
After=network.target postgresql.service

[Service]
Type=simple
User=mockapi
Group=mockapi
WorkingDirectory=/opt/mockup-api-server
EnvironmentFile=/opt/mockup-api-server/.env
ExecStart=/usr/bin/java -jar \
  -Xms512m \
  -Xmx2048m \
  -Dspring.profiles.active=prod \
  /opt/mockup-api-server/mockup-api-server.jar

Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=mockup-api-server

[Install]
WantedBy=multi-user.target
```

**2. Create User and Directory:**

```bash
sudo useradd -r -s /bin/false mockapi
sudo mkdir -p /opt/mockup-api-server
sudo cp mockup-api-server.jar /opt/mockup-api-server/
sudo cp .env /opt/mockup-api-server/
sudo chown -R mockapi:mockapi /opt/mockup-api-server
```

**3. Enable and Start Service:**

```bash
sudo systemctl daemon-reload
sudo systemctl enable mockup-api-server
sudo systemctl start mockup-api-server
sudo systemctl status mockup-api-server
```

**4. View Logs:**

```bash
sudo journalctl -u mockup-api-server -f
```

---

### Method 2: Docker Deployment

**1. Create Dockerfile:**

```dockerfile
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="your-email@example.com"

# Create app directory
WORKDIR /app

# Copy JAR file
COPY build/libs/mockup-api-server.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
```

**2. Build Docker Image:**

```bash
./gradlew clean bootJar
docker build -t mockup-api-server:1.0.0 .
```

**3. Create docker-compose.yml:**

```yaml
version: '3.8'

services:
  app:
    image: mockup-api-server:1.0.0
    container_name: mockup-api-server
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=mockdb
      - DB_USERNAME=mockuser
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - postgres
    networks:
      - mockup-network

  postgres:
    image: postgres:15-alpine
    container_name: mockup-postgres
    restart: unless-stopped
    environment:
      - POSTGRES_DB=mockdb
      - POSTGRES_USER=mockuser
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - mockup-network

  nginx:
    image: nginx:alpine
    container_name: mockup-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    networks:
      - mockup-network

volumes:
  postgres_data:

networks:
  mockup-network:
    driver: bridge
```

**4. Run with Docker Compose:**

```bash
docker-compose up -d
docker-compose logs -f app
```

---

### Method 3: Cloud Deployment (AWS Example)

**1. Elastic Beanstalk:**

```bash
# Install EB CLI
pip install awsebcli

# Initialize
eb init -p java-17 mockup-api-server

# Create environment
eb create production-env \
  --instance-type t3.small \
  --envvars JWT_SECRET=your_secret,DB_PASSWORD=your_password

# Deploy
eb deploy
```

**2. EC2 Instance:**

```bash
# SSH to instance
ssh -i your-key.pem ubuntu@your-instance-ip

# Install Java
sudo apt update
sudo apt install openjdk-17-jre-headless

# Copy JAR
scp -i your-key.pem mockup-api-server.jar ubuntu@your-instance-ip:~

# Follow systemd deployment steps
```

---

## Monitoring and Maintenance

### 1. Health Checks

**Actuator Endpoints:**

```bash
# Health
curl http://localhost:8080/actuator/health

# Info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics
```

---

### 2. Logging

**Configure Log Rotation:**

**/etc/logrotate.d/mockup-api-server:**

```
/var/log/mockup-api-server/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 mockapi mockapi
    sharedscripts
    postrotate
        systemctl reload mockup-api-server > /dev/null 2>&1 || true
    endscript
}
```

---

### 3. Backup

**Database Backup Script:**

```bash
#!/bin/bash
BACKUP_DIR="/backups/mockdb"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup
pg_dump -U mockuser -h localhost mockdb > "$BACKUP_DIR/mockdb_$DATE.sql"

# Compress
gzip "$BACKUP_DIR/mockdb_$DATE.sql"

# Keep only last 7 days
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
```

**Cron Job:**

```bash
# Daily backup at 2 AM
0 2 * * * /opt/scripts/backup-db.sh
```

---

## Performance Optimization

### 1. JVM Tuning

```bash
java -jar \
  -Xms1024m \
  -Xmx2048m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/mockup-api-server \
  mockup-api-server.jar
```

### 2. Database Optimization

```sql
-- Add indexes
CREATE INDEX CONCURRENTLY idx_mock_endpoints_expiration 
  ON mock_endpoints(expiration_date) 
  WHERE expiration_date IS NOT NULL;

-- Analyze tables
ANALYZE users;
ANALYZE projects;
ANALYZE mock_endpoints;
```

### 3. Nginx Reverse Proxy

**nginx.conf:**

```nginx
upstream mockup_api {
    server localhost:8080;
}

server {
    listen 80;
    server_name api.yourcompany.com;
    
    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourcompany.com;
    
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Gzip compression
    gzip on;
    gzip_types application/json application/xml text/plain;
    
    # Proxy settings
    location / {
        proxy_pass http://mockup_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## Troubleshooting

### Application Won't Start

**Check Logs:**
```bash
sudo journalctl -u mockup-api-server -n 100
```

**Common Issues:**
- Port 8080 already in use → Change port
- Database connection failed → Check credentials
- Out of memory → Increase heap size

---

### Database Connection Issues

**Test Connection:**
```bash
psql -h localhost -U mockuser -d mockdb
```

**Check Firewall:**
```bash
sudo ufw status
sudo ufw allow 5432/tcp
```

---

### High Memory Usage

**Check Memory:**
```bash
free -h
ps aux | grep java
```

**Reduce Heap:**
```bash
-Xmx1024m  # Instead of -Xmx2048m
```

---

## Conclusion

Follow this guide to deploy the Mockup API Server to production. Remember to:

✅ Secure environment variables  
✅ Use production database  
✅ Enable HTTPS  
✅ Set up monitoring  
✅ Configure backups  
✅ Review security checklist

For questions, consult the [SECURITY.md](SECURITY.md) documentation.
