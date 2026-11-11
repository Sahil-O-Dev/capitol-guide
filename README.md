# Capitol Guide

A website to help people discover and learn about their congressional representatives.

Status: The React frontend is connected to the Spring Boot health endpoint. The backend can import congressional member summaries and selected detail fields into PostgreSQL and return saved members filtered by state and chamber. Map browsing and member profile pages are not implemented yet.

## Member list API

With the backend running on port 8080 and member summaries already imported, request:

```text
http://localhost:8080/api/members?state=Virginia&chamber=Senate
http://localhost:8080/api/members?state=Virginia&chamber=House%20of%20Representatives
```

Both `state` and `chamber` are required; omitting either returns HTTP 400. Use the state name and chamber exactly as stored, such as `Virginia`, `Senate`, or `House of Representatives`. `%20` encodes a space in the URL.

The endpoint returns a JSON array of matching members from our database. No matches returns `[]`. Each member includes `bioguideId`, `name`, `partyName`, `state`, `chamber`, `district`, `imageUrl`, and `imageAttribution`; unavailable values can be null. Visitor requests do not trigger a Congress.gov import.

The importer stores `officialWebsiteUrl`, `currentMember`, and `sourceUpdateDate` from each member's detail response. These fields are not yet included in the list response, and a local profile endpoint is not implemented.

## Import congressional members

Set `DB_PASSWORD` and `CONGRESS_API_KEY` in the backend process environment. Obtain a Congress.gov API key from the [official signup page](https://api.congress.gov/sign-up/). Keep both values out of source control, frontend code, and logs.

From `backend/`, using Java 21, run:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=congress-import
```

This starts the application, applies pending migrations, and runs the importer against the real Congress.gov API. It writes to the configured PostgreSQL database. Normal startup without this profile does not import members.

The import has two stages:

1. Fetch summaries with `currentMember=true`, then fetch details for each summary's Bioguide ID. Check that the IDs match and combine the fields into `Member` objects in memory.
2. After all members are prepared, save them together in one database transaction. Existing members are updated using their Bioguide IDs.

A failed request (including HTTP 429 rate limiting), mismatched/missing ID, or mapping error stops preparation before any member writes. Database runtime failures roll back the save transaction. Errors propagate; automatic retries are not implemented. A missing or blank source date is stored as null; a malformed date aborts preparation. `sourceUpdateDate` is Congress.gov's record update time, not the time of our last successful import.

The client requests up to 250 summaries per page and advances the offset by 250 while the response reports another page. This is the [documented maximum page size](https://github.com/LibraryOfCongress/api.congress.gov#limit-and-offset). Member detail requests are separate requests. Imports do not delete members missing from a later source list; removal of former members and last-successful-import tracking remain future work.

## Backend tests

From `backend/`, using Java 21, run `./mvnw test` with PostgreSQL running and `DB_PASSWORD` and `CONGRESS_API_KEY` set. Tests do not call Congress.gov; the API key may be a dummy value. Leave `congress-import` and `congress-preview` profiles disabled during tests.

Tests cover detail mapping, stable-ID pairing, preparation failures, runner ordering, pagination, database persistence, and rollback after a database constraint failure. API responses are mocked and test member records are fictional. Most database tests roll back their changes; the transaction-failure test commits a fictional seed record and removes it in cleanup. Prefer a dedicated test database by overriding `SPRING_DATASOURCE_URL` and, if needed, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`.

## Local database requirements

The backend requires Java 21 and a running PostgreSQL server.

The local configuration expects:

- PostgreSQL at `localhost:5432`
- A database named `capitol_guide`
- A login role named `capitol_guide` with permission to connect to the database and create tables in its schema
- A `DB_PASSWORD` environment variable containing that role's password

Set `DB_PASSWORD` in the environment of whichever process runs the application or integration tests. In IntelliJ, application and test run configurations may need this setting separately. Keep the password out of source control.

Flyway applies versioned SQL migrations when the application starts. Hibernate validates that the resulting schema matches the Java entities.

`MemberRepositoryTest` and `MemberImportServiceTest` use the configured PostgreSQL database; see the test section above for their data handling.

Database creation and complete application run instructions are still to be documented.
