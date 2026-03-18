# Feature Documentation

## Authentication

**Flow summary**
1. `POST /api/auth/register` creates user, logs audit event, returns access+refresh tokens.
2. `POST /api/auth/login` validates password, checks lockout, logs audit, returns tokens.
3. `POST /api/auth/refresh` verifies old refresh token, revokes it, issues a new access+refresh token.
4. `POST /api/auth/logout` revokes tokens for current user.

**Key classes**
- `AuthController`
- `AuthServiceImpl`
- `JwtService`, `JwtAuthenticationFilter`
- `RefreshTokenServiceImpl`
- `CustomUserDetailsService`, `SecurityUtils`

**Design decisions**
- Access tokens are short-lived; refresh tokens are rotated for safety.
- Security events are stored in a dedicated audit table to enable later analysis.

## Auditing

There are three layers of auditing:

1. **Security audit logs**
   - `SecurityAuditService` writes to `security_audit_logs`.
   - Events include login success/failure, token refresh, logout, account status changes.
   - Writes asynchronously so it does not slow the main request.

2. **Business event logs**
   - `BusinessEventService` writes to `business_event_logs`.
   - Events include extraction, moderation actions, user message lifecycle, signal engine/normalization, insights views, admin overview views, test data seeds.
   - Writes asynchronously so it does not slow the main request.

3. **Domain audit events**
   - `AuditTrailListener` listens for domain events (e.g., `ItemCreatedEvent`) and logs them.

**Why multiple layers**
- Security audit logs are structured, searchable, and persist in DB.
- Business event logs capture product/ops signals without mixing with security events.
- Domain audit logs are light-weight operational signals via logging.

## Tracing

- `TraceIdFilter` sets or generates `X-Trace-Id` per request.
- The trace ID is injected into MDC and included in log patterns (`logback-spring.xml`).
- All logs can be correlated by this ID.

Snippet:
```java
String traceId = request.getHeader("X-Trace-Id");
if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();
MDC.put(TRACE_ID, traceId);
response.setHeader("X-Trace-Id", traceId);
```

## Rate Limiting

- Implemented as a `OncePerRequestFilter`.
- Uses a per-path policy (`RateLimitPolicyProvider`).
- Key is either user email (if authenticated) or client IP.
- Returns HTTP 429 with JSON error body.

Limit strategy is **fixed window in-memory**, which is simple but not cluster-safe.

## API Flow (end-to-end)

1. **TraceIdFilter** assigns/propagates a trace ID.
2. **RateLimitFilter** enforces per-endpoint limits.
3. **JwtAuthenticationFilter** authenticates if Bearer token is present.
4. **Controller** validates input and calls a service.
5. **Service** applies business logic, metrics, audits, and repository calls.
6. **Repository** reads/writes the database.

```
HTTP request
  -> TraceIdFilter
  -> RateLimitFilter
  -> JwtAuthenticationFilter
  -> Controller
  -> Service (+ metrics/audit/events)
  -> Repository -> DB
  -> HTTP response
```

## Metrics (PdpMetrics)

- Centralized metrics registry for business KPIs.
- Exposed via `/actuator/prometheus`.
- Used in services to count events and measure duration.

This directly answers the "PdpMetrics" question: it is the place you track product-level counters (logins, item created, extraction requests, insights calls) and key latency timers.
