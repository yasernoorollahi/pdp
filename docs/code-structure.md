# Code Structure

## Top-level

- `src/main/java/com/datarain/pdp`: Java source code
- `src/main/resources`: configuration and migrations
- `src/test/java`: tests
- `docs`: documentation (this folder)
- `infra`: infrastructure files (e.g., Prometheus)

## Main packages (by responsibility)

### `auth`
- Authentication flow (login/register/refresh/logout)
- Refresh token management

### `admin`
- Admin monitoring endpoints and system overview

### `audit`
- Domain audit listeners (e.g., item created events)

### `common`
- Base entities and value objects
- Domain event base types

### `config`
- Spring configuration: CORS, JPA auditing, OpenAPI, etc.

### `exception`
- Business exceptions and global error handling

### `extraction`
- AI extraction endpoints and service layer
- External AI extraction HTTP client and DTOs

### `infrastructure`
Infrastructure concerns and cross-cutting services:

- `security`: JWT, auth filter, password encoding, roles
- `security/audit`: security event audit logging
- `security/lockout`: account lockout logic
- `logging`: trace ID filter for request correlation
- `metrics`: Micrometer counters and timers (PdpMetrics)
- `rate_limit`: rate limiting filter, policy, and service
- `external`: HTTP clients (e.g., AI extraction)
- `job`: background jobs and monitoring
- `health`: health indicators

### `insights`
- Aggregation and API exposure for insight views

### `item`
- CRUD API for items + domain events

### `message`
- User message ingestion, analysis, and processing status

### `moderation`
- Moderation cases, transitions, admin APIs

### `notification`
- Notifications and scheduling

### `signal`
- Signal normalization and AI pipeline orchestration

### `testdata`
- Data seeding services

### `user`
- User entity, service, controller, repository

## Resources

- `application.yml`: runtime configuration
- `db/migration`: Flyway migrations
- `logback-spring.xml`: logging configuration

## Tests

- Unit tests in `src/test/java/com/datarain/pdp/service/*`
- Integration tests under `src/test/java/com/datarain/pdp/it` and `.../integration`
