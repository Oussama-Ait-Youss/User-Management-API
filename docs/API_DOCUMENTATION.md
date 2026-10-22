# API Documentation

Base URL (Tomcat deployment): `http://localhost:8080/User_Management_API_war_exploded`

All endpoints are intentionally unauthenticated. In Postman, set **Authorization → No Auth**. This API is for learning only and is not production-ready.

| Method | URL | Success |
|---|---|---|
| GET | `/` | 200, application running information |
| GET | `/api/health` | 200, health information |
| POST | `/api/users` | 201, created user and `Location` header |
| GET | `/api/users` | 200, list of active users |
| GET | `/api/users/{id}` | 200, active user |
| PUT | `/api/users/{id}` | 200, updated user |
| DELETE | `/api/users/{id}` | 204, logically deactivated |
| GET | `/api/users/role/{role}` | 200, active users with role `ADMIN` or `USER` |
| PATCH | `/api/users/{id}/password` | 200, safe user response after changing password |

## Create a user

`name`, `email`, and `password` are required. Email must be valid and unique. Passwords must contain 8 or more characters and no more than 72 UTF-8 bytes (the BCrypt input limit). `role` is optional and defaults to `USER`; otherwise it must be `ADMIN` or `USER`.

```json
{
  "name": "System Admin",
  "email": "admin@example.com",
  "password": "Admin12345!",
  "role": "ADMIN"
}
```

The response contains `id`, `name`, `email`, `role`, `active`, `createdAt`, and `updatedAt`. It never contains the password or password hash.

## Update a user

`PUT /api/users/{id}` accepts only `name` and `email`. Password and role cannot be changed through this request.

```json
{
  "name": "Updated Name",
  "email": "updated@example.com"
}
```

## Change a password

`PATCH /api/users/{id}/password` accepts `currentPassword` and `newPassword`. It returns the same safe user-response fields as other successful user operations. An incorrect current password returns HTTP 400. This check does not provide authentication or authorization.

```json
{
  "currentPassword": "Admin12345!",
  "newPassword": "NewAdmin12345!"
}
```

## Error response

Errors use this shape:

```json
{
  "timestamp": "2026-10-22T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users",
  "fieldErrors": [
    { "field": "email", "message": "Email must be valid" }
  ]
}
```

HTTP 400 indicates invalid input or an incorrect current password; 404 indicates a missing or inactive user/route; 409 indicates duplicate email; 500 indicates an unexpected server error. Ordinary operations do not return 401 or 403.
