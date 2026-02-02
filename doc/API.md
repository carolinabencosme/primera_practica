# API Documentation

This document describes the REST API endpoints available in the Mockup API Server.

## Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Mock Execution API](#mock-execution-api)
- [Error Responses](#error-responses)
- [Examples](#examples)

## Overview

The Mockup API Server provides a dynamic mock execution API that routes requests to configured mock endpoints based on project name, path, and HTTP method.

### Base URL

```
http://localhost:8080
```

### Content Types

- Request: `application/json`, `application/xml`, `text/plain`, or any configured type
- Response: Configured per mock endpoint (default: `application/json`)

## Authentication

### Form-Based Login (Web UI)

The web interface uses Spring Security form-based authentication.

**Endpoint**: `POST /login`

**Request** (Form Data):
```
username=admin
password=admin
```

**Response**:
- Success: Redirect to `/` with session cookie
- Failure: Redirect to `/login?error`

**Session Cookie**: `JSESSIONID`

---

### JWT Authentication (API)

Mock endpoints can optionally require JWT authentication. When a mock endpoint has `requiresJwt=true`, you must include a valid JWT token.

**Note**: The current implementation validates JWT tokens but does not expose a public login endpoint to generate them. JWT generation is handled internally by `JwtService`.

#### JWT Token Structure

```json
{
  "sub": "username",
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Token Expiration**: 24 hours (86400000 ms)

**Secret Key**: Configured in `application.properties` (change in production!)

#### Using JWT Tokens

Include the token in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token Validation**:
- Signature verification (HMAC-SHA)
- Expiration check
- Username extraction

---

## Mock Execution API

### Execute Mock Endpoint

Dynamically executes a configured mock endpoint.

**Endpoint**: `[GET|POST|PUT|PATCH|DELETE|OPTIONS] /api/mock/{projectName}/{path}`

**Parameters**:
- `projectName` (path): Name of the project containing the mock
- `path` (path): Endpoint path (can include multiple segments)

**Headers** (optional):
- `Authorization: Bearer <token>` (required if mock has `requiresJwt=true`)
- `Content-Type: application/json` (for POST/PUT/PATCH requests)

**Request Body** (for POST/PUT/PATCH):
- Any valid payload (not validated, passed through)

**Response**:
- **Status Code**: As configured in mock endpoint (e.g., 200, 404, 500)
- **Headers**:
  - `Content-Type`: As configured (default: `application/json`)
  - Custom headers from `MockHeader` entities
- **Body**: Response body from mock configuration

**Response Behaviors**:
1. **Delay**: If `delaySeconds > 0`, response delayed by configured seconds
2. **Expiration**: If past `expirationDate`, returns `410 GONE`
3. **JWT Validation**: If `requiresJwt=true`, validates JWT (401 if invalid/missing)
4. **Not Found**: If no matching mock exists, returns `404 NOT FOUND`

---

### Users Shortcut Endpoint

The `/api/users` endpoint is a shortcut that proxies to the mock project configured via
`mock.users.project-name` in `application.properties`.

**Configuration**:

```properties
mock.users.project-name=Usuarios
```

**Example**:

```bash
curl -X GET http://localhost:8080/api/users
```

This is equivalent to:

```bash
curl -X GET http://localhost:8080/api/mock/Usuarios/api/users
```

---

### Examples

#### Example 1: Simple GET Request

**Mock Configuration**:
- Project: `UserAPI`
- Path: `/users/1`
- Method: `GET`
- Status: `200`
- Response:
  ```json
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
  ```

**Request**:
```bash
curl -X GET http://localhost:8080/api/mock/UserAPI/users/1
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

---

#### Example 2: POST Request with Delay

**Mock Configuration**:
- Project: `OrderAPI`
- Path: `/orders`
- Method: `POST`
- Status: `201`
- Delay: `2` seconds
- Response:
  ```json
  {
    "orderId": "12345",
    "status": "created"
  }
  ```

**Request**:
```bash
curl -X POST http://localhost:8080/api/mock/OrderAPI/orders \
     -H "Content-Type: application/json" \
     -d '{"product": "Widget", "quantity": 5}'
```

**Response** (after 2-second delay):
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "orderId": "12345",
  "status": "created"
}
```

---

#### Example 3: JWT-Protected Endpoint

**Mock Configuration**:
- Project: `SecureAPI`
- Path: `/admin/secrets`
- Method: `GET`
- Status: `200`
- Requires JWT: `true`
- Response:
  ```json
  {
    "secret": "confidential data"
  }
  ```

**Request WITHOUT JWT**:
```bash
curl -X GET http://localhost:8080/api/mock/SecureAPI/admin/secrets
```

**Response**:
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "JWT token is missing or invalid"
}
```

**Request WITH JWT**:
```bash
curl -X GET http://localhost:8080/api/mock/SecureAPI/admin/secrets \
     -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "secret": "confidential data"
}
```

---

#### Example 4: Custom Headers

**Mock Configuration**:
- Project: `RateLimitAPI`
- Path: `/api/status`
- Method: `GET`
- Status: `200`
- Headers:
  - `X-Rate-Limit-Remaining`: `99`
  - `X-Request-ID`: `abc123`
- Response:
  ```json
  {"status": "ok"}
  ```

**Request**:
```bash
curl -v http://localhost:8080/api/mock/RateLimitAPI/api/status
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Rate-Limit-Remaining: 99
X-Request-ID: abc123

{"status": "ok"}
```

---

#### Example 5: Error Response (404)

**Mock Configuration**:
- Project: `ErrorAPI`
- Path: `/not-found`
- Method: `GET`
- Status: `404`
- Response:
  ```json
  {
    "error": "Resource not found",
    "code": "NOT_FOUND"
  }
  ```

**Request**:
```bash
curl -X GET http://localhost:8080/api/mock/ErrorAPI/not-found
```

**Response**:
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "Resource not found",
  "code": "NOT_FOUND"
}
```

---

#### Example 6: Expired Endpoint

**Mock Configuration**:
- Project: `TempAPI`
- Path: `/temporary`
- Method: `GET`
- Expiration: `2024-01-01T00:00:00` (past date)

**Request**:
```bash
curl -X GET http://localhost:8080/api/mock/TempAPI/temporary
```

**Response**:
```http
HTTP/1.1 410 Gone
Content-Type: application/json

{
  "error": "This mock endpoint has expired"
}
```

---

#### Example 7: Endpoint Not Found

**Request** (no matching mock):
```bash
curl -X GET http://localhost:8080/api/mock/UnknownProject/some/path
```

**Response**:
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "Mock endpoint not found for GET /some/path in project UnknownProject"
}
```

---

#### Example 8: XML Response

**Mock Configuration**:
- Project: `LegacyAPI`
- Path: `/users/1`
- Method: `GET`
- Status: `200`
- Content-Type: `application/xml`
- Response:
  ```xml
  <user>
    <id>1</id>
    <name>John Doe</name>
  </user>
  ```

**Request**:
```bash
curl -X GET http://localhost:8080/api/mock/LegacyAPI/users/1
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/xml

<user>
  <id>1</id>
  <name>John Doe</name>
</user>
```

---

#### Example 9: Nested Path

**Mock Configuration**:
- Project: `ComplexAPI`
- Path: `/v1/users/123/orders/456`
- Method: `GET`
- Status: `200`
- Response:
  ```json
  {
    "userId": 123,
    "orderId": 456,
    "total": 99.99
  }
  ```

**Request**:
```bash
curl -X GET http://localhost:8080/api/mock/ComplexAPI/v1/users/123/orders/456
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "userId": 123,
  "orderId": 456,
  "total": 99.99
}
```

---

## Error Responses

### Standard Error Format

```json
{
  "error": "Error message",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00"
}
```

### HTTP Status Codes

| Code | Description | Scenario |
|------|-------------|----------|
| `200` | OK | Successful mock response (if configured) |
| `201` | Created | Resource creation mock (if configured) |
| `400` | Bad Request | Invalid request format |
| `401` | Unauthorized | Missing or invalid JWT token |
| `403` | Forbidden | Insufficient permissions |
| `404` | Not Found | Mock endpoint doesn't exist |
| `410` | Gone | Mock endpoint expired |
| `500` | Internal Server Error | Server error or mock configured with 500 |

### Common Error Scenarios

#### 1. Mock Endpoint Not Found

**Request**:
```bash
curl http://localhost:8080/api/mock/NonExistentProject/path
```

**Response**:
```http
HTTP/1.1 404 Not Found

{
  "error": "Mock endpoint not found for GET /path in project NonExistentProject"
}
```

---

#### 2. JWT Required but Missing

**Request**:
```bash
curl http://localhost:8080/api/mock/SecureAPI/protected
```

**Response**:
```http
HTTP/1.1 401 Unauthorized

{
  "error": "JWT token is required for this endpoint"
}
```

---

#### 3. Invalid JWT Token

**Request**:
```bash
curl -H "Authorization: Bearer invalid.token.here" \
     http://localhost:8080/api/mock/SecureAPI/protected
```

**Response**:
```http
HTTP/1.1 401 Unauthorized

{
  "error": "Invalid JWT token"
}
```

---

#### 4. Expired JWT Token

**Request**:
```bash
curl -H "Authorization: Bearer expired.jwt.token" \
     http://localhost:8080/api/mock/SecureAPI/protected
```

**Response**:
```http
HTTP/1.1 401 Unauthorized

{
  "error": "JWT token has expired"
}
```

---

#### 5. Mock Endpoint Expired

**Response**:
```http
HTTP/1.1 410 Gone

{
  "error": "This mock endpoint has expired on 2024-01-01"
}
```

---

## Advanced Usage

### Simulating Latency

Configure `delaySeconds` to simulate slow APIs:

```json
{
  "delaySeconds": 5
}
```

This will pause for 5 seconds before returning the response.

### Simulating Temporary Endpoints

Set `expirationDate` for time-limited mocks:

```json
{
  "expirationDate": "2024-12-31T23:59:59"
}
```

After this date, requests return `410 GONE`.

### Testing Error Scenarios

Configure mocks with error status codes:

```json
{
  "httpStatusCode": 500,
  "responseBody": "{\"error\": \"Internal server error\"}"
}
```

### Multi-Tenant API Mocking

Create separate projects for different clients:

- Project: `ClientA_API` → `/api/mock/ClientA_API/**`
- Project: `ClientB_API` → `/api/mock/ClientB_API/**`

Each can have identical paths but different responses.

---

## Best Practices

### 1. Consistent Project Naming

Use clear, descriptive project names:
- `UserManagementAPI`
- `PaymentGateway`
- `NotificationService`

### 2. RESTful Path Conventions

Follow REST conventions:
- `GET /users` - List users
- `GET /users/{id}` - Get single user
- `POST /users` - Create user
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user

### 3. Use Appropriate Status Codes

- `200` - Successful GET
- `201` - Successful POST (created)
- `204` - Successful DELETE (no content)
- `400` - Validation error
- `404` - Not found
- `500` - Server error

### 4. JSON Formatting

Use properly formatted JSON for responses:

```json
{
  "data": {
    "id": 1,
    "attributes": {}
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### 5. Custom Headers for Metadata

Add headers for additional information:
- `X-Request-ID` - Request tracking
- `X-Rate-Limit-Remaining` - Rate limit info
- `X-Total-Count` - Pagination total
- `Cache-Control` - Caching directives

### 6. Document Your Mocks

Include descriptions in mock endpoint configurations to document intended use cases and expected behaviors.

---

## Limitations

1. **No Request Body Validation**: Mock endpoints return configured responses regardless of request body content
2. **No Query Parameter Support**: Query parameters are ignored (only path matching)
3. **Static Responses**: Responses are static; no dynamic data generation
4. **No Request Logging**: Incoming requests are not logged/stored
5. **No Rate Limiting**: No built-in rate limiting on mock execution
6. **Single Response Per Endpoint**: Each endpoint has one response (no conditional logic)

---

## Future Enhancements

Potential improvements for the API:

- **Public JWT Login Endpoint**: `/api/auth/login` for programmatic token generation
- **Request Body Validation**: JSON schema validation
- **Dynamic Responses**: Templating engine for variable substitution
- **Request Logging**: Store and view mock request history
- **Response Sequences**: Return different responses on subsequent calls
- **Conditional Logic**: Return different responses based on request data
- **GraphQL Support**: Mock GraphQL queries and mutations
- **WebSocket Support**: Mock real-time endpoints

---

## Conclusion

The Mock Execution API provides a flexible way to simulate REST APIs without writing code. By configuring projects and endpoints through the web UI, you can quickly create realistic API responses for development, testing, and prototyping.
