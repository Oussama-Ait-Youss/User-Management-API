# Project Audit

## Implemented application shape

The project is a Spring MVC 6 / Spring Data JPA WAR for Tomcat 10.1. It uses Java 17, Hibernate 6, file-backed H2, HikariCP, DTO-based JSON endpoints, a service layer, and centralized exception responses. Spring Boot is not used.

## API behavior

- CRUD operations create users, list active users, read/update by ID, and logically deactivate users.
- Active users can be filtered by the database-only `ADMIN` or `USER` role.
- User passwords are BCrypt hashed; passwords and hashes are excluded from responses.
- The dedicated password endpoint checks the existing password before replacing the hash.
- All endpoints are intentionally public, including ADMIN creation and user management. This is for learning only and is not production-ready.
- `/` and `/api/health` return public status information.

## Database and deployment

H2 stores its file under `%USERPROFILE%\\.user-management-api\\data`, a stable absolute user-home location independent of the Tomcat/IDE working directory. `mvn package` produces `target/User_Management_API_war_exploded.war`, which deploys at `/User_Management_API_war_exploded`.

## Verification

Executed checks and any environment limitations are recorded in [TEST_RESULTS.md](./TEST_RESULTS.md).
