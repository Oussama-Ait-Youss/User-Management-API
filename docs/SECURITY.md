# Security and learning-only limitations

This API intentionally has no authentication or authorization. Every endpoint is directly accessible without a username, password, JWT, Basic Auth, login session, or role check. A caller can create an `ADMIN` user and can read, update, deactivate, or change the password of any user by ID. The `role` value is stored as a normal database attribute only.

This behavior is intentionally insecure for learning purposes. Do not deploy this API to a public or production environment.

Passwords are still hashed with BCrypt before persistence. Password hashes are not included in response DTOs or application logs. A password-change request verifies the supplied current password against the stored hash, but knowledge of a user ID and current password is not authentication or authorization. BCrypt accepts at most 72 UTF-8 bytes; longer new passwords are rejected with HTTP 400.

The default H2 data file is under `%USERPROFILE%\\.user-management-api\\data`. Its default `sa` account has a blank password and is appropriate only for local learning.
