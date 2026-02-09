# authentication-service

Multi-tenant authentication service built with Spring Boot 3 and Gradle.

## Features

- **Multi-tenant Architecture**: Complete tenant isolation with `tenant_id` in the database
- **JWT Authentication**: Secure token-based authentication with `user_id` and `tenant_id` claims
- **BCrypt Password Hashing**: Industry-standard password encryption
- **Repository Pattern**: Flexible database abstraction layer
- **Firebase Admin SDK Integration**: Support for Firebase token verification (optional)
- **Unique Constraint**: Email uniqueness per tenant `(tenant_id, email)`
- **RESTful API**: Simple and clean API design

## Technology Stack

- **Spring Boot**: 3.2.2
- **Java**: 17
- **Gradle**: 8.5
- **Database**: H2 (in-memory) / PostgreSQL
- **Security**: Spring Security, BCrypt
- **JWT**: JJWT 0.12.3
- **Firebase**: Firebase Admin SDK 9.2.0

## Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 8.5 or higher (or use the included Gradle wrapper)

### Build

```bash
./gradlew clean build
```

### Run

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## API Endpoints

All authentication endpoints require the `X-Tenant-ID` header.

### Register

Register a new user in a specific tenant.

**Endpoint**: `POST /auth/register`

**Headers**:
- `X-Tenant-ID`: Tenant identifier (required)
- `Content-Type`: application/json

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "1",
  "tenantId": "tenant1",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login

Authenticate an existing user.

**Endpoint**: `POST /auth/login`

**Headers**:
- `X-Tenant-ID`: Tenant identifier (required)
- `Content-Type`: application/json

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "1",
  "tenantId": "tenant1",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

## Multi-Tenant Isolation

Users are completely isolated by tenant. The same email address can be registered in different tenants:

```bash
# Register user@example.com in tenant1
curl -X POST http://localhost:8080/auth/register \
  -H "X-Tenant-ID: tenant1" \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "pass1"}'

# Register user@example.com in tenant2 (different user)
curl -X POST http://localhost:8080/auth/register \
  -H "X-Tenant-ID: tenant2" \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "pass2"}'
```

## JWT Token

JWT tokens contain the following claims:
- `user_id`: The user's unique identifier
- `tenant_id`: The tenant identifier
- `sub`: Subject (same as user_id)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp (24 hours from issue)

## Configuration

Edit `src/main/resources/application.yml` to configure:

- Database connection (H2 or PostgreSQL)
- JWT secret and expiration
- Firebase settings (optional)
- Logging levels

### Environment Variables

For production deployments, use environment variables instead of hardcoded values:

- `JWT_SECRET`: Your secure JWT secret key (must be at least 256 bits for HS256)
- `JWT_EXPIRATION`: Token expiration time in milliseconds (default: 86400000 = 24 hours)
- `FIREBASE_ENABLED`: Enable/disable Firebase integration (default: false)
- `FIREBASE_SERVICE_ACCOUNT_PATH`: Path to Firebase service account JSON file

Example:
```bash
export JWT_SECRET="your-production-secret-key-minimum-256-bits-long"
export JWT_EXPIRATION=86400000
./gradlew bootRun
```

### Firebase (Optional)

To enable Firebase token verification:

1. Set `firebase.enabled: true` in `application.yml`
2. Provide the path to your Firebase service account JSON file
3. The service will only verify tokens (not create users)

## Error Responses

The API returns consistent error responses:

**400 Bad Request** (Missing tenant ID):
```json
{
  "message": "X-Tenant-ID header is required",
  "status": 400,
  "timestamp": 1770644592116
}
```

**401 Unauthorized** (Invalid credentials):
```json
{
  "message": "Invalid email or password",
  "status": 401,
  "timestamp": 1770644602661
}
```

**409 Conflict** (Duplicate email in tenant):
```json
{
  "message": "User already exists with email: user@example.com",
  "status": 409,
  "timestamp": 1770644537443
}
```

## Development

### H2 Console

When running with H2 database, access the console at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:authdb`
- Username: `sa`
- Password: (empty)

### Testing

Run tests with:
```bash
./gradlew test
```

## Security Notes

- The default JWT secret in `application.yml` is for development only
- Change the JWT secret in production to a secure random value
- BCrypt automatically handles salt generation for password hashing
- Firebase integration is optional and disabled by default
