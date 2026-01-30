# User Guide

Welcome to the Mockup API Server User Guide! This document provides step-by-step instructions for using the application to create and manage mock API endpoints.

## Table of Contents

- [Getting Started](#getting-started)
- [Logging In](#logging-in)
- [Managing Projects](#managing-projects)
- [Managing Mock Endpoints](#managing-mock-endpoints)
- [Testing Mock Endpoints](#testing-mock-endpoints)
- [User Management (Admin)](#user-management-admin)
- [FAQ](#faq)
- [Troubleshooting](#troubleshooting)

## Getting Started

### Prerequisites

Before you begin, ensure:
1. The application is running (see [README.md](README.md) for installation)
2. You have login credentials (default: `admin` / `admin`)
3. A web browser (Chrome, Firefox, Safari, Edge)

### First Login

1. Open your browser and navigate to: **http://localhost:8080**
2. You'll be redirected to the login page
3. Enter your credentials
4. Click **"Login"**

---

## Logging In

### Step 1: Access the Login Page

Navigate to http://localhost:8080/login

![Login Page]

### Step 2: Enter Credentials

**Default Admin Credentials:**
- **Username**: `admin`
- **Password**: `admin`

**⚠️ IMPORTANT**: Change the default password after first login!

### Step 3: Navigate the Dashboard

After login, you'll see the home page with navigation options:
- **Projects** - Manage your mock API projects
- **Mocks** - View all mock endpoints
- **Users** (Admin only) - Manage system users
- **Logout** - End your session

---

## Managing Projects

Projects are logical containers for grouping related mock endpoints (e.g., `UserAPI`, `OrderAPI`).

### Create a New Project

#### Step 1: Navigate to Projects

Click **"Projects"** in the navigation menu or go to http://localhost:8080/projects

#### Step 2: Click "Create New Project"

Click the **"Create New Project"** button

#### Step 3: Fill in Project Details

**Required Fields:**
- **Name**: Unique project name (URL-safe, no spaces)
  - Example: `UserManagementAPI`
  - Used in URL: `/api/mock/UserManagementAPI/...`
  
**Optional Fields:**
- **Description**: Describe the project's purpose
  - Example: "Mock endpoints for user CRUD operations"

#### Step 4: Save the Project

Click **"Save"** or **"Create Project"**

**Success**: You'll be redirected to the project list

**Example Project:**
```
Name: UserAPI
Description: Mock endpoints for user management system
Created By: admin
Created At: 2024-01-15 10:30:00
```

---

### View Project Details

1. Go to **Projects** page
2. Click on a project name
3. View:
   - Project information
   - List of mock endpoints in this project
   - Options to add/edit/delete endpoints

---

### Edit a Project

1. Navigate to the project details page
2. Click **"Edit"** button
3. Modify name or description
4. Click **"Save"**

**Note**: Changing the project name affects all mock endpoint URLs!

---

### Delete a Project

1. Navigate to the project details page
2. Click **"Delete"** button
3. Confirm deletion

**⚠️ WARNING**: Deleting a project will also delete **ALL** its mock endpoints!

---

## Managing Mock Endpoints

Mock endpoints define the API responses for specific paths and HTTP methods.

### Create a Mock Endpoint

#### Step 1: Navigate to Project

1. Go to **Projects**
2. Click on the project where you want to add the endpoint

#### Step 2: Click "Add Mock Endpoint"

Click the **"Add Mock Endpoint"** or **"Create New Mock"** button

#### Step 3: Configure Basic Settings

**Required Fields:**

**1. Path** (e.g., `/users/1`, `/api/orders`)
- Must start with `/`
- Can include multiple segments: `/v1/users/123/orders`
- Supports dynamic-looking paths: `/users/{id}` (treated as literal)

**2. HTTP Method** (dropdown)
- `GET` - Retrieve data
- `POST` - Create data
- `PUT` - Update (full replacement)
- `PATCH` - Update (partial)
- `DELETE` - Delete data
- `OPTIONS` - CORS preflight

**3. HTTP Status Code** (default: `200`)
- `200` - OK
- `201` - Created
- `204` - No Content
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error
- Any valid HTTP status code

#### Step 4: Configure Response

**Response Body** (optional, text area)
- JSON example:
  ```json
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "active": true
  }
  ```
- XML example:
  ```xml
  <user>
    <id>1</id>
    <name>John Doe</name>
  </user>
  ```
- Plain text:
  ```
  Success: User created
  ```

**Content Type** (default: `application/json`)
- `application/json` - JSON data
- `application/xml` - XML data
- `text/plain` - Plain text
- `text/html` - HTML content
- Custom types supported

#### Step 5: Configure Advanced Options

**Expiration Date** (optional)
- Set a date/time when the mock should stop working
- Format: `YYYY-MM-DDTHH:MM:SS`
- Example: `2024-12-31T23:59:59`
- After expiration, endpoint returns `410 GONE`

**Delay (seconds)** (default: `0`)
- Simulate slow API responses
- Example: `3` = 3-second delay before response
- Useful for testing loading states and timeouts

**Requires JWT** (checkbox, default: unchecked)
- Check if endpoint should require JWT authentication
- Clients must include `Authorization: Bearer <token>` header
- Returns `401 Unauthorized` if token missing/invalid

#### Step 6: Add Custom Headers (Optional)

Click **"Add Header"** to include custom response headers:

**Common Examples:**
- **Header Key**: `X-Request-ID`  
  **Header Value**: `abc-123-def-456`

- **Header Key**: `X-Rate-Limit-Remaining`  
  **Header Value**: `99`

- **Header Key**: `Cache-Control`  
  **Header Value**: `no-cache`

- **Header Key**: `X-Custom-Data`  
  **Header Value**: `any-value-here`

#### Step 7: Save the Mock Endpoint

Click **"Save"** or **"Create Mock Endpoint"**

**Success**: Endpoint is now available at:
```
[METHOD] http://localhost:8080/api/mock/{ProjectName}/{Path}
```

---

### Example Mock Configurations

#### Example 1: Get User by ID

```
Project: UserAPI
Path: /users/1
Method: GET
Status: 200
Content-Type: application/json
Response Body:
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "user"
}

URL: GET http://localhost:8080/api/mock/UserAPI/users/1
```

---

#### Example 2: Create Order (with delay)

```
Project: OrderAPI
Path: /orders
Method: POST
Status: 201
Content-Type: application/json
Delay: 2 seconds
Response Body:
{
  "orderId": "ORD-12345",
  "status": "pending",
  "createdAt": "2024-01-15T10:30:00Z"
}

URL: POST http://localhost:8080/api/mock/OrderAPI/orders
```

---

#### Example 3: Protected Admin Endpoint

```
Project: AdminAPI
Path: /admin/settings
Method: GET
Status: 200
Requires JWT: ✓ (checked)
Content-Type: application/json
Response Body:
{
  "settings": {
    "maintenance": false,
    "debugMode": true
  }
}

URL: GET http://localhost:8080/api/mock/AdminAPI/admin/settings
Header: Authorization: Bearer <your-jwt-token>
```

---

#### Example 4: Temporary Endpoint (expires)

```
Project: PromoAPI
Path: /holiday-sale
Method: GET
Status: 200
Expiration Date: 2024-12-31T23:59:59
Response Body:
{
  "discount": 50,
  "message": "Happy Holidays! 50% off everything!"
}

URL: GET http://localhost:8080/api/mock/PromoAPI/holiday-sale
(Returns 410 GONE after Dec 31, 2024)
```

---

#### Example 5: Error Response

```
Project: ErrorAPI
Path: /not-found
Method: GET
Status: 404
Response Body:
{
  "error": "Resource not found",
  "code": "RESOURCE_NOT_FOUND",
  "timestamp": "2024-01-15T10:30:00Z"
}

URL: GET http://localhost:8080/api/mock/ErrorAPI/not-found
```

---

### View Mock Endpoint Details

1. Go to **Projects** → Select a project
2. Click on a mock endpoint from the list
3. View all configuration details

---

### Edit a Mock Endpoint

1. Navigate to the mock endpoint details page
2. Click **"Edit"** button
3. Modify any fields
4. Click **"Save"**

**Tip**: You can change response bodies, status codes, and settings without affecting the URL.

---

### Delete a Mock Endpoint

1. Navigate to the mock endpoint details page
2. Click **"Delete"** button
3. Confirm deletion

**Note**: This only deletes the mock, not the entire project.

---

## Testing Mock Endpoints

### Using cURL (Command Line)

#### Basic GET Request

```bash
curl http://localhost:8080/api/mock/UserAPI/users/1
```

#### POST Request with Body

```bash
curl -X POST http://localhost:8080/api/mock/OrderAPI/orders \
     -H "Content-Type: application/json" \
     -d '{"product": "Widget", "quantity": 5}'
```

#### Request with JWT Token

```bash
curl http://localhost:8080/api/mock/SecureAPI/protected \
     -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### View Response Headers

```bash
curl -v http://localhost:8080/api/mock/UserAPI/users/1
```

---

### Using Postman

1. **Create New Request**
   - Method: Select HTTP method (GET, POST, etc.)
   - URL: `http://localhost:8080/api/mock/{ProjectName}/{Path}`

2. **Add Headers** (if needed)
   - `Authorization: Bearer <token>`
   - `Content-Type: application/json`

3. **Add Body** (for POST/PUT/PATCH)
   - Select **"raw"** and **"JSON"**
   - Enter request body

4. **Send Request**
   - Click **"Send"**
   - View response status, headers, and body

---

### Using Web Browser

For `GET` requests, simply paste the URL:

```
http://localhost:8080/api/mock/UserAPI/users/1
```

Browser will display the JSON response.

**Note**: POST/PUT/DELETE require tools like Postman or cURL.

---

### Verify Response Behavior

Check that your mock returns:
- ✅ Correct status code
- ✅ Expected response body
- ✅ Custom headers (use `-v` flag in cURL)
- ✅ Delay applied (time the request)
- ✅ JWT validation working (401 if missing)
- ✅ Expiration working (410 after expiration date)

---

## User Management (Admin)

**Note**: Only users with `ROLE_ADMIN` can manage users.

### Create a New User

#### Step 1: Navigate to Users

Click **"Users"** in the navigation menu (Admin only)

Or go to http://localhost:8080/users

#### Step 2: Click "Create New User"

Click the **"Create New User"** button

#### Step 3: Fill in User Details

**Required Fields:**
- **Username**: Unique username for login
  - Example: `jdoe`
  - No spaces, alphanumeric recommended

- **Password**: User's password
  - Minimum 6 characters recommended
  - Encrypted with BCrypt before storage

- **Email**: User's email address
  - Must be unique
  - Example: `jdoe@example.com`

**Optional Fields:**
- **Enabled**: Check to activate account (default: checked)
- **Roles**: Select user roles
  - `ROLE_USER` - Standard access
  - `ROLE_ADMIN` - Full system access

#### Step 4: Save the User

Click **"Save"** or **"Create User"**

**Success**: User can now log in with provided credentials

---

### Edit a User

1. Go to **Users** page
2. Click on a username
3. Click **"Edit"** button
4. Modify details (username, email, roles, enabled status)
5. Click **"Save"**

**Note**: Password is only updated if you enter a new one.

---

### Disable a User

1. Edit the user
2. Uncheck **"Enabled"** checkbox
3. Save

**Result**: User cannot log in (gets authentication error)

---

### Delete a User

1. Go to user details page
2. Click **"Delete"** button
3. Confirm deletion

**⚠️ WARNING**: 
- User's projects and mock endpoints may remain (depending on configuration)
- Cannot delete yourself while logged in

---

### Grant Admin Access

1. Edit the user
2. Check **"ROLE_ADMIN"** in the roles section
3. Save

**Result**: User gains access to user management and admin features

---

## FAQ

### General Questions

**Q: What is a mock API endpoint?**  
A: A mock endpoint simulates a real API response without backend logic. Useful for frontend development, testing, and prototyping.

**Q: Can I use this in production?**  
A: Not recommended. This is designed for development/testing. For production mocks, consider dedicated API mocking services.

**Q: Are requests to mock endpoints logged?**  
A: Currently, no. Request logging is not implemented.

**Q: Can I mock GraphQL APIs?**  
A: No, only REST APIs are supported.

---

### Project Questions

**Q: Can two projects have the same name?**  
A: No, project names must be unique across the system.

**Q: Can I rename a project?**  
A: Yes, but it will change the URLs of all mock endpoints in that project.

**Q: What happens if I delete a project?**  
A: All mock endpoints within that project are also deleted (CASCADE delete).

---

### Mock Endpoint Questions

**Q: Can I have multiple endpoints with the same path?**  
A: Yes, if they use different HTTP methods (e.g., `GET /users` and `POST /users`).

**Q: Can I use path parameters like `/users/{id}`?**  
A: Path parameters are treated as literal strings. `/users/{id}` matches only exactly that path, not `/users/123`.

**Q: How do I simulate pagination?**  
A: Create multiple endpoints (e.g., `/users?page=1`, `/users?page=2`) or use a single endpoint with a paginated response body.

**Q: Can responses be dynamic (e.g., current timestamp)?**  
A: No, responses are static. You must manually update the response body to change values.

**Q: What's the maximum response body size?**  
A: Limited by database TEXT field (typically 64KB-16MB depending on database). For large responses, consider splitting.

---

### Authentication Questions

**Q: How do I get a JWT token?**  
A: Currently, JWT tokens are generated internally by the system. A public login API endpoint would be needed for external token generation (future enhancement).

**Q: Can I test JWT-protected endpoints without a token?**  
A: No, you'll receive a `401 Unauthorized` error. You need a valid JWT token.

**Q: How long are JWT tokens valid?**  
A: 24 hours (86400000 ms) from issuance.

**Q: Can I invalidate a JWT token?**  
A: JWT tokens are stateless. There's no built-in revocation mechanism (would require token blacklist).

---

### Performance Questions

**Q: Is there a rate limit?**  
A: No, mock endpoints have no rate limiting. Use `delaySeconds` to throttle responses if needed.

**Q: How many mock endpoints can I create?**  
A: No hard limit, but performance may degrade with thousands of endpoints. Database queries would slow down.

**Q: Does the delay block other requests?**  
A: Yes, delays use `Thread.sleep()`, which blocks the request thread. High delays can exhaust thread pool.

---

## Troubleshooting

### Login Issues

**Problem**: Cannot log in with default credentials

**Solutions**:
1. Verify application is running: `http://localhost:8080`
2. Check credentials are correct: `admin` / `admin`
3. Check browser console for errors
4. Try clearing browser cookies/cache
5. Verify H2 database is initialized (check logs for `DataInitializer`)

---

**Problem**: "User is disabled" error

**Solution**: Admin must re-enable your user account via User Management.

---

### Mock Endpoint Not Working

**Problem**: `404 Not Found` when calling mock endpoint

**Solutions**:
1. Verify project name in URL matches exactly (case-sensitive)
2. Verify path matches exactly (including leading `/`)
3. Verify HTTP method matches (GET vs POST, etc.)
4. Check endpoint exists: go to Projects → View project → Check endpoint list
5. Check for typos in URL

**Example**:
- ❌ `http://localhost:8080/api/mock/userapi/users/1` (wrong case)
- ✅ `http://localhost:8080/api/mock/UserAPI/users/1` (correct)

---

**Problem**: `410 Gone` error

**Solution**: Mock endpoint has expired. Edit the endpoint and:
- Remove expiration date, or
- Set a future expiration date

---

**Problem**: `401 Unauthorized` error

**Solutions**:
1. Verify endpoint requires JWT (check "Requires JWT" setting)
2. Include JWT token in request: `Authorization: Bearer <token>`
3. Verify token is valid (not expired)
4. Check token format (must start with "Bearer ")

---

**Problem**: Response is delayed or slow

**Solution**: Check endpoint's `delaySeconds` setting. If set to `5`, response takes 5 seconds. Edit endpoint to reduce or remove delay.

---

### Application Issues

**Problem**: Application won't start

**Solutions**:
1. Check Java version: `java -version` (need Java 17+)
2. Check port 8080 is available: `lsof -i :8080` (macOS/Linux)
3. Check Gradle build succeeded: `./gradlew build`
4. Check application logs for errors

---

**Problem**: H2 Console not accessible

**Solutions**:
1. Verify `spring.h2.console.enabled=true` in `application.properties`
2. Navigate to: `http://localhost:8080/h2-console`
3. Use JDBC URL: `jdbc:h2:mem:mockdb`
4. Username: `sa`, Password: (blank)

---

**Problem**: Changes not reflecting

**Solutions**:
1. Hard refresh browser: `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac)
2. Clear browser cache
3. Check if you're editing the correct project/endpoint
4. Verify changes were saved (check confirmation message)

---

### Data Issues

**Problem**: Lost all mock data after restart

**Solution**: H2 in-memory database resets on restart. To persist data:
1. Change to file-based H2: `jdbc:h2:file:./mockdb`
2. Or use external database (PostgreSQL, MySQL)

---

**Problem**: Cannot delete project

**Solution**: Verify you're the creator or have admin privileges. Check for foreign key constraints.

---

## Getting Help

If you encounter issues not covered here:

1. **Check Logs**: Application logs show detailed error messages
   - Console output during `./gradlew bootRun`
   - Look for stack traces and error messages

2. **Check Database**: Use H2 console to inspect data
   - Verify records exist in `project` and `mock_endpoint` tables

3. **Review Documentation**:
   - [API.md](API.md) - API endpoint details
   - [DATABASE.md](DATABASE.md) - Database schema
   - [ARCHITECTURE.md](ARCHITECTURE.md) - System design

4. **Contact Support**: Reach out to your development team

---

## Best Practices

1. **Use descriptive project names**: Makes URLs readable
2. **Document your mocks**: Use project descriptions
3. **Test immediately**: Verify mock works after creation
4. **Use realistic data**: Mock responses should match real API format
5. **Set expiration dates**: For temporary testing scenarios
6. **Change default password**: Security first!
7. **Use appropriate status codes**: Match HTTP semantics
8. **Version your APIs**: Include version in path (e.g., `/v1/users`)

---

## Conclusion

You now have all the knowledge to effectively use the Mockup API Server! Create projects, define mock endpoints, and test your applications without a real backend. Happy mocking!
