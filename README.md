# Capitol Guide

A website to help people discover and learn about their congressional representatives.

Status: The React frontend is connected to the Spring Boot health endpoint. The backend can import congressional member summaries into PostgreSQL and return saved members filtered by state and chamber. Map browsing and member profile pages are not implemented yet.

## Member list API

With the backend running on port 8080 and member summaries already imported, request:

```text
http://localhost:8080/api/members?state=Virginia&chamber=Senate
http://localhost:8080/api/members?state=Virginia&chamber=House%20of%20Representatives
```

Both `state` and `chamber` are required; omitting either returns HTTP 400. Use the state name and chamber exactly as stored, such as `Virginia`, `Senate`, or `House of Representatives`. `%20` encodes a space in the URL.

The endpoint returns a JSON array of matching members from our database. No matches returns `[]`. Each member includes `bioguideId`, `name`, `partyName`, `state`, `chamber`, `district`, `imageUrl`, and `imageAttribution`; unavailable values can be null. Visitor requests do not trigger a Congress.gov import.

The Congress.gov client also has methods to fetch one member's detailed information or collect details using IDs from the member list. Detail responses are not yet saved to PostgreSQL or exposed through a local profile endpoint. The single-member request has a mocked client test; the full detail collection flow has not yet been verified.

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
