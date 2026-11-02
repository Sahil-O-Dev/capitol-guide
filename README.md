# Capitol Guide

A website to help people discover and learn about their congressional representatives.

Status: React frontend connected to the Spring Boot health endpoint. Congressional browsing and data integration are not implemented yet.

## Local database requirements

The backend requires Java 21 and a running PostgreSQL server.

The local configuration expects:

- PostgreSQL at `localhost:5432`
- A database named `capitol_guide`
- A login role named `capitol_guide` with permission to connect to the database and create tables in its schema
- A `DB_PASSWORD` environment variable containing that role's password

Set `DB_PASSWORD` in the environment of whichever process runs the application or integration tests. In IntelliJ, application and test run configurations may need this setting separately. Keep the password out of source control.

Flyway applies versioned SQL migrations when the application starts. Hibernate validates that the resulting schema matches the Java entities.

`MemberRepositoryTest` uses the configured PostgreSQL database. Its test data is fictional, and its database changes are rolled back after each test.

Database creation and complete application run instructions are still to be documented.
