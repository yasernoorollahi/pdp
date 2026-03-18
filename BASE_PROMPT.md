# PDP Base Prompt (Use Before Any New Work)

This file is the authoritative summary of project conventions. Use it as the baseline before making any new changes.

## Options (Edit In Place)
Use the examples below to decide defaults for new work. Edit the chosen line(s) directly.

```
Access policy (default for new endpoints): USER + ADMIN
Rate limit policy: add/adjust in RateLimitPolicyProvider
Metrics: add counter/timer when business-relevant
Security audit: log when action is security-sensitive
Domain event: publish when action is auditable/observable
Business event: log when action is product/ops-relevant
```

## Goal
New code must align with the layered architecture, security, observability, and existing coding style.

## Architecture and Layering (Strict)
- Controller -> Service -> Repository
- DTOs for all request/response payloads
- No business logic inside Controllers
- Use existing `BaseEntity` / `AuditableEntity` when applicable
- Use validation annotations (e.g., `@Valid`, `@NotNull`)
- Global exception handling only (local try/catch only when truly needed)
- Pagination for list endpoints using `Pageable`
- Follow existing naming conventions

## Security and Access Control
- JWT + RBAC enforced. Apply both route config and `@PreAuthorize`.
- Routes in `SecurityConfig`:
  - `POST /api/auth/register`, `/api/auth/login`, `/api/auth/refresh` are public
  - Actuator health is public, other actuator endpoints require ADMIN
  - Swagger is public
  - All other routes require authentication

### Access Policy (Choose Per Endpoint)
Add this comment above the access decision and pick one:
```
// Access policy (choose one):
// - ADMIN only
// - USER + ADMIN
// - PUBLIC (auth not required)
// - SYSTEM (internal service role)
```

## Rate Limiting (Global Filter)
- Rate limiting is enforced by `RateLimitFilter`.
- Policies are path-based in `RateLimitPolicyProvider`.

### Rate Limit Option
```
// Rate limit: add/adjust policy for this path in RateLimitPolicyProvider
```

## Metrics (PdpMetrics)
- Counters and timers live in `PdpMetrics`.
- Services increment/record at meaningful points.

### Metrics Option
```
// Metrics: add new counter/timer in PdpMetrics if this action is meaningful
```

## Security Audit Logs
- Sensitive/security-relevant actions should be logged in `SecurityAuditService`.
- Event types live in `SecurityEventType`.

### Security Audit Option
```
// Security audit: log a SecurityEventType for this action if relevant
```

## Business Event Logs
- Product/ops actions should be logged in `BusinessEventService`.
- Event types live in `BusinessEventType`.

### Business Event Option
```
// Business event: log a BusinessEventType for this action if relevant
```

## Domain Audit / Events
- Domain events (e.g., `ItemCreatedEvent`) are published in services.
- Listeners like `AuditTrailListener` / analytics / notifications consume them.

### Domain Event Option
```
// Domain event: publish if this action should be auditable/observable
```

## Logging and TraceId
- TraceId is set by `TraceIdFilter` into MDC.
- Use structured logging in services (e.g., `log.atInfo().addKeyValue(...)`).

## Service Design Standards
- Services are the business logic boundary.
- Services should handle validation, metrics, audits, domain events, and logging.
- Repositories only handle persistence/query logic.

## Tests
- Add an integration test skeleton for new APIs.
- Add unit tests for core logic where applicable.

## Checklist for Adding a New API
- Controller with DTOs
- Service interface + implementation
- Repository (if needed)
- Mapper (if mapping is non-trivial)
- Custom exception (if needed)
- SecurityConfig access review
- RateLimitPolicyProvider policy review
- PdpMetrics counter/timer (if needed)
- SecurityAuditService event (if needed)
- BusinessEventService event (if needed)
- Domain event (if needed)
- Integration test

## Notes
- If a new feature conflicts with existing behavior, ask before changing.
- All changes must align with the existing project structure and patterns.
