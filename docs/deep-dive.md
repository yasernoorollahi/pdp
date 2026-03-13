# Deep Dive Documentation

This document explains the most important classes with method-level guidance and design reasoning.

## SecurityConfig
File: `src/main/java/com/datarain/pdp/infrastructure/security/SecurityConfig.java`

### Purpose
Defines Spring Security filter chain and access rules for the API.

### Key decisions
- **Stateless** sessions because JWT is used.
- **CSRF, formLogin, httpBasic disabled** because the app is pure REST.
- **Actuator**: health is public; other endpoints require `ROLE_ADMIN`.
- **Swagger** is public to simplify API exploration.
- **Custom filters**: rate limiting and JWT auth are placed before `UsernamePasswordAuthenticationFilter`.

### Core configuration snippet
```java
http
  .csrf(csrf -> csrf.disable())
  .formLogin(form -> form.disable())
  .httpBasic(basic -> basic.disable())
  .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
      .requestMatchers(EndpointRequest.to("health")).permitAll()
      .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("ROLE_ADMIN")
      .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
      .anyRequest().authenticated()
  )
  .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
  .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

### Why this design
- Filters are placed **before** username/password processing to short-circuit early if needed.
- Access rules are explicit and easy to reason about.

## JWT Authentication

### JwtService
File: `src/main/java/com/datarain/pdp/infrastructure/security/jwt/JwtService.java`

**Responsibilities**
- Generate access tokens
- Extract username (email)
- Validate token expiration and subject

**Key methods**
- `generateAccessToken(User user)`: creates JWT with `subject=email`, `userId`, and `roles` claims, with HS256 signing.
- `extractUsername(String token)`: reads subject from claims.
- `isTokenValid(String token, UserDetails userDetails)`: matches username and checks expiration.

**Design reasoning**
- The token includes `userId` and `roles` to avoid extra DB lookups where possible.
- Secret and expiration come from configuration, not hardcoded.

### JwtAuthenticationFilter
File: `src/main/java/com/datarain/pdp/infrastructure/security/jwt/JwtAuthenticationFilter.java`

**Flow**
1. Read `Authorization` header.
2. If not `Bearer ...`, pass through.
3. Extract username from JWT.
4. If SecurityContext is empty, load user and validate token.
5. Build `UsernamePasswordAuthenticationToken` and set it on SecurityContext.

Snippet:
```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}

jwt = authHeader.substring(7);
userEmail = jwtService.extractUsername(jwt);

if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
    if (jwtService.isTokenValid(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
```

### CustomUserDetails / CustomUserDetailsService
Files:
- `.../security/CustomUserDetails.java`
- `.../security/CustomUserDetailsService.java`

**Why**
- Stores `userId` in the principal so services can access it without another DB call.
- Maps `ROLE_*` values from DB directly to `GrantedAuthority`.

### Refresh tokens
File: `src/main/java/com/datarain/pdp/auth/service/impl/RefreshTokenServiceImpl.java`

**Flow**
- `create`: create new token (7 days) for device+IP.
- `verify`: check revoked and expiration.
- `rotate`: revoke old and create new (30 days).
- `revoke*`: mark token(s) revoked.

### AuthServiceImpl
File: `src/main/java/com/datarain/pdp/auth/service/impl/AuthServiceImpl.java`

**register**
- Validate uniqueness.
- Save user with BCrypt hash and `ROLE_USER`.
- Log security audit.
- Create refresh token + access token.

**login**
- Validate user existence and password.
- Lockout and audit on failures.
- Reset lockout on success.
- Create refresh token + access token.
- Record metrics and timing.

**refresh**
- Verify old refresh token.
- Revoke old, rotate to new.
- Generate new access token.
- Audit and metrics.

**logout/logoutAll**
- Revoke refresh tokens for user.
- Audit.

## Rate Limiting

### RateLimitFilter
File: `src/main/java/com/datarain/pdp/infrastructure/rate_limit/filter/RateLimitFilter.java`

**Flow**
1. Resolve policy based on request path.
2. Resolve key: authenticated user email or IP address.
3. Call `RateLimitService.checkRateLimit(...)`.
4. If limit exceeded, return `429` JSON response and increment metric.

Snippet:
```java
RateLimitConfig config = policyProvider.resolve(request);
String key = resolveKey(request);
rateLimitService.checkRateLimit(key, config);
```

### RateLimitPolicyProvider
File: `.../rate_limit/config/RateLimitPolicyProvider.java`

- Defines per-endpoint limits, from specific to general.
- Example: `/api/auth/login` has the strictest policy.

### InMemoryRateLimitService
File: `.../rate_limit/service/impl/InMemoryRateLimitService.java`

- Uses in-memory counters with a fixed window.
- Synchronized per key to keep correctness.
- Simple but resets on restart and does not scale horizontally.

### RedisRateLimitService
File: `.../rate_limit/service/impl/RedisRateLimitService.java`

- Placeholder class (not implemented).
- Intended to show that multiple implementations can exist.

## Controllers (pattern)

Controllers are thin and call a service. They enforce method-level security via `@PreAuthorize`.

### Example: ItemController
File: `src/main/java/com/datarain/pdp/item/controller/ItemController.java`

- `POST /api/items`: create item (ROLE_USER/ROLE_ADMIN)
- `GET /api/items/{id}`: get item (ROLE_USER/ROLE_ADMIN)
- `DELETE /api/items/{id}`: archive item (ROLE_ADMIN)
- `GET /api/items`: list/search items with paging
- `PUT /api/items/{id}/restore`: restore item (ROLE_ADMIN)

### Example: UserMessageController
File: `src/main/java/com/datarain/pdp/message/controller/UserMessageController.java`

- CRUD for user messages, plus processing flags.
- Uses current user context for ownership checks in service layer.

### Example: ExtractionController
File: `src/main/java/com/datarain/pdp/extraction/controller/ExtractionController.java`

- Admin-only endpoints to call AI extraction by type.
- `classify` allows ROLE_USER/ROLE_SYSTEM for analysis pipeline.

### Example: InsightsController
File: `src/main/java/com/datarain/pdp/insights/controller/InsightsController.java`

- Provides timeline and summary analytics using daily metrics.

## Services (pattern + examples)

### ItemServiceImpl
File: `src/main/java/com/datarain/pdp/item/service/impl/ItemServiceImpl.java`

- `create`: map DTO -> entity, save, publish `ItemCreatedEvent`, increment metrics, timer.
- `getById`: fetch or throw `ItemNotFoundException`.
- `delete`: archive item (soft delete), publish `ItemArchivedEvent`.
- `getAll`: specification query: status+enabled+type+search.
- `restore`: unarchive.

### UserMessageServiceImpl
File: `src/main/java/com/datarain/pdp/message/service/impl/UserMessageServiceImpl.java`

- `create`: save new message with initial processing state, audit + metrics.
- `update`: reset analysis state and retry count after edits.
- `setProcessed`: mark as processed and audit.
- `delete`: remove message and audit.
- `getAll`: filter by processed and date range using specifications.
- `analyzePendingMessages`: batch process pending messages through extraction.

### ExtractionServiceImpl
File: `src/main/java/com/datarain/pdp/extraction/service/impl/ExtractionServiceImpl.java`

- Wraps all extraction endpoints with `invokeEndpoint(...)`.
- Centralizes metrics, audit, and logging for all extraction calls.
- Uses `SecurityUtils` when available; falls back to system actor.

### InsightsServiceImpl
File: `src/main/java/com/datarain/pdp/insights/service/impl/InsightsServiceImpl.java`

- Reads `DailyBehaviorMetric` data and converts to trend responses.
- Adds audit logs and per-endpoint metrics.
- Uses `TraceIdFilter` to correlate logs.

### AuthServiceImpl (security + metrics)
See Auth section above for method detail.

## Metrics (PdpMetrics)
File: `src/main/java/com/datarain/pdp/infrastructure/metrics/PdpMetrics.java`

- A central registry of counters and timers used by services.
- Exposed via `/actuator/prometheus` for dashboards.
- Used by Auth, Item, Extraction, Signal Engine, Insights, Moderation, etc.

Example usage in `ItemServiceImpl`:
```java
metrics.getItemCreatedCounter().increment();
metrics.getItemCreateTimer().record(Duration.between(startedAt, Instant.now()));
```
