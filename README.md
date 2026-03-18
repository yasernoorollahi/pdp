# Personal Data Platform (PDP)

A production-style Spring Boot backend for secure user data management, AI-powered message understanding, moderation workflows, and operational observability.

## Table of Contents

- [Overview](#overview)
- [Core Capabilities](#core-capabilities)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [AI Processing Pipeline](#ai-processing-pipeline)
- [Scheduled Jobs](#scheduled-jobs)
- [API Surface](#api-surface)
- [Data Model](#data-model)
- [Security Model](#security-model)
- [Observability](#observability)
- [Local Development](#local-development)
- [Docker Deployment](#docker-deployment)
- [Testing](#testing)
- [Production Hardening Checklist](#production-hardening-checklist)
- [Project Structure](#project-structure)

## Overview

PDP is a layered Java 21 / Spring Boot 3.5 service that provides:

- JWT auth + refresh token lifecycle
- Role-based access (`ROLE_USER`, `ROLE_ADMIN`, internal `ROLE_SYSTEM` paths)
- Item lifecycle management with domain events
- User message ingestion and asynchronous AI enrichment
- AI signal storage and admin analytics endpoints
- Moderation case management
- Rate limiting, audit logging, health/metrics endpoints, and job monitoring


## Engineering Case Study

A deep dive into the architecture and design decisions behind PDP.

👉 [Read the Engineering Case Study](docs/engineering-case-study.md)


## Core Capabilities

- Authentication: register, login, refresh, logout, logout-all
- User management: profile, admin user controls (enable/unlock)
- Item APIs: CRUD-like flows with archive/restore behavior
- Extraction APIs: facts/intent/tone/context/cognitive/topics/classify/signals
- AI signal engine: batch pipeline over useful user messages
- Moderation: admin-managed case lifecycle (approve/reject/auto-block)
- Monitoring: business stats, system overview, recent job execution logs

## Tech Stack

- Java 21
- Spring Boot 3.5.10
- Spring Web, Validation, Data JPA, Security, Actuator
- PostgreSQL + Flyway migrations
- JWT (`jjwt`)
- MapStruct + Lombok
- Micrometer + Prometheus
- Resilience4j
- OpenAPI/Swagger (`springdoc`)
- Docker / Docker Compose
- Test stack: JUnit 5, Spring Test, Spring Security Test, Rest Assured, Testcontainers

## System Architecture

The project follows a clean layered modular design per domain:

- `controller` -> HTTP/API boundary
- `service` -> use-case/business logic
- `repository` -> persistence and external integration
- `entity` / `dto` / `mapper` / `specification` -> model + transport + query composition
- `infrastructure` -> security, jobs, metrics, external clients, health, rate limiting

### High-Level Flow

```text
Client -> REST Controllers -> Domain Services ->
  (JPA Repositories / External AI Client)
       -> PostgreSQL / AI Provider

Cross-cutting:
  Security (JWT + RBAC)
  Rate Limiting
  Security Audit Logs
  Metrics + Health + Job Monitoring
```

### Event-Driven Side Flows

`ItemCreatedEvent` is published by the item service and consumed by listeners for:

- notification preparation
- behavioral analytics logging
- audit trail logging

## AI Processing Pipeline

PDP has a two-stage async pipeline for user messages:

1. `UserMessageAnalysisJob`
- Reads `user_messages` with `analysis_status = PENDING`
- Calls `/extract/classify`
- Sets `analysis_status`, `signal_decision`, `signal_score`, `signal_reason`
- Marks `processing_status`:
  - `PENDING` if classified as useful (to be processed by signal engine)
  - `DONE` if ignored/skipped

2. `AiSignalEngineJob`
- Picks processable useful messages (`PENDING` / `FAILED`, retry-aware)
- Calls orchestrator to fetch facts/intent/tone/context/cognitive/topics
- Stores normalized snapshot into `message_signals` (`signals` JSONB)
- Updates message processing state (`DONE` or `FAILED`, with retry count)

## Scheduled Jobs

| Job | Purpose | Trigger |
|---|---|---|
| `PurgeExpiredRefreshTokensJob` | Deletes expired refresh tokens | Every 1 minute |
| `ExpireArchivedItemsJob` | Deletes archived items older than retention cutoff | Every 1 minute |
| `NotificationEmailJob` | Marks pending notifications as sent | Every hour |
| `UserMessageAnalysisJob` | Classification stage for pending messages | `jobs.user-message-analysis.cron` (default: every 2 min) |
| `AiSignalEngineJob` | Signal extraction stage for useful messages | `jobs.ai-signal-engine.cron` (default: every 3 min) |
| `TestDataSeedingJob` | Seeds synthetic dataset on startup (when enabled) | `ApplicationRunner` |

All monitored jobs write structured execution data to `job_execution_log` via `JobMonitoringService` and publish metrics.

## API Surface

Base path groups:

- `/api/auth` (public + authenticated logout flows)
- `/api/users` (admin + self)
- `/api/items`
- `/api/user-messages`
- `/api/extraction`
- `/api/admin` (monitoring)
- `/api/admin/ai-signal-engine`
- `/api/admin/moderation/cases`

Docs and ops endpoints:

- Swagger UI: `/swagger` (backed by `/v3/api-docs`)
- Actuator: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`, etc.

## Data Model

Main tables (via Flyway migrations):

- Identity/security: `users`, `user_roles`, `refresh_tokens`, `security_audit_logs`
- Business/ops audit: `business_event_logs`
- Core content: `items`, `notifications`, `moderation_cases`
- AI/message pipeline: `user_messages`, `message_signals`
- Analytics foundation: `daily_behavior_metrics`, `user_entities`, `user_activities`, `user_topics`, `intent_items`, `user_preferences`, `cognitive_states`
- Ops: `job_execution_log`

Schema design highlights:

- UUID primary keys
- audit columns (`created_at`, `updated_at`, etc.)
- JSONB storage for extracted signals
- indexes for job scans and query-heavy filters
- status constraints for pipeline and moderation states

## Security Model

- Stateless JWT authentication (`Authorization: Bearer ...`)
- Refresh token rotation + revocation
- Role/authority checks in route config + method-level `@PreAuthorize`
- Login lockout policy after repeated failures
- Async security audit trail persisted in DB
- Request rate limiting (policy-by-path)

## Observability

- Micrometer custom counters/timers (`pdp.*`)
- Prometheus scraping through `/actuator/prometheus`
- Grafana in docker-compose for dashboards
- Job success/failure/duration metrics
- Admin monitoring APIs for business + system overview

## Local Development

### Prerequisites

- JDK 21
- Docker (recommended for infra)
- Maven wrapper (`./mvnw`)

### Run Infra

```bash
docker compose up -d postgres redis
```

### Run Application

```bash
./mvnw spring-boot:run
```

Or build and run jar:

```bash
./mvnw clean package
java -jar target/personal-data-platform-0.0.1-SNAPSHOT.jar
```

## Docker Deployment

The project includes:

- Multi-stage `Dockerfile` (build + slim runtime)
- `docker-compose.yml` with `app`, `postgres`, `redis`, `prometheus`, `grafana`

Run full stack:

```bash
docker compose up --build
```

## Testing

Run tests:

```bash
./mvnw test
```

The test suite includes unit, integration, MockMvc, and Testcontainers-based flows.

## Production Hardening Checklist

Before publishing or deploying publicly:

- Replace all default credentials and secrets
- Move secrets to env/secret manager (never commit)
- Disable or tightly control test-data seeding in production
- Restrict Swagger/Actuator exposure by environment
- Ensure `logs/` and `target/` are excluded from commits
- Review migration seed data (default admin seed policy)
- Tune rate limits and lockout thresholds for your traffic profile

## Project Structure

```text
src/main/java/com/datarain/pdp
  auth/             # auth + tokens
  user/             # user admin and self profile
  item/             # item lifecycle + events
  message/          # user messages + analysis state
  extraction/       # AI extraction endpoints/integration
  signal/           # signal orchestration + storage
  moderation/       # moderation case management
  notification/     # notification preparation + dispatch job
  admin/            # monitoring APIs
  infrastructure/   # security, jobs, metrics, health, external clients
  common/           # base entities/events/value objects
  exception/        # error model + global handling
```

---

If you plan to publish this repository on GitHub, run a final secret scan and remove any local runtime artifacts before pushing.

#checking codex

test push
