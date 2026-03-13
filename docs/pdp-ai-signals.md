# PDP AI Signals (TypeScript Fastify)

This document explains the `pdp-ai-signals` service (stateless extraction microservice) and how it connects to the core backend.

Project path:
- `/Users/yaser/Downloads/@PDP/working on pdp with AI tools/pdp-ai-signals`

## Architecture Overview

- **Runtime**: Node 20+, TypeScript
- **Web framework**: Fastify
- **Configuration**: `dotenv` + `zod` validation (`env.ts`)
- **Providers**: OpenAI, Ollama, or Mock
- **Validation**: Zod schemas for all extraction outputs
- **Observability**: request logging + structured errors
- **API docs**: Swagger/OpenAPI at `/api-docs`

High-level flow:

```
HTTP request -> controller -> parse/validate input -> service -> AI client -> provider -> JSON output
                                         |-> schema validation -> response
```

## Folder Structure

Key folders under `src/`:

- `controllers/` — HTTP endpoints for each extraction capability
- `services/` — extraction and orchestration logic
- `orchestrator/` — multi-signal aggregation
- `adapters/ai-providers/` — OpenAI/Ollama/Mock providers
- `prompts/` — prompt templates per capability
- `schemas/` — strict Zod schemas for each output
- `config/` — env loading, provider factory, DI container, swagger
- `utils/` — error handling, retries, request logging
- `interfaces/` — contracts for services

## Server bootstrap

Entry point: `src/server.ts`

- Builds a Fastify app with request logging hooks
- Registers Swagger
- Registers all controllers
- Registers a global error handler

```ts
const app = Fastify({ logger: true, requestTimeout: config.REQUEST_TIMEOUT_MS });
app.addHook('onRequest', ...);
app.addHook('onResponse', ...);
await registerSwagger(app, config.PORT);
container.signalsController.register(app);
registerErrorHandler(app);
```

## Dependency container

`AppContainer` creates and wires dependencies:

- Loads config
- Builds `ExtractionServiceFactory`
- Instantiates controllers and request logger

This keeps `server.ts` clean and makes components easy to test.

## Configuration (`env.ts`)

Config is strict and validated at startup:

- `AI_PROVIDER`: `openai | ollama | mock`
- `AI_MODEL`: default model name
- `OPENAI_API_KEY` required when using OpenAI
- `REQUEST_TIMEOUT_MS`, retry settings, etc.

If config is invalid, the app fails fast.

## Provider selection

`createProvider(...)` decides which provider to use per request or global default:

- OpenAI provider uses `/chat/completions` and expects JSON content.
- Ollama provider uses `/api/generate`.
- Mock provider returns deterministic responses for local testing.

Provider overrides are passed from controller request body.

## Extraction services

All extraction services inherit from `BaseExtractionService` which:

1. Builds a prompt.
2. Calls AI provider.
3. Parses strict JSON.
4. Validates with a Zod schema.
5. Returns a normalized `SignalEnvelope`.

```ts
const raw = await aiClient.generate(prompt);
const parsed = parseStrictJson(raw);
const validated = envelopeSchema.safeParse(parsed);
```

This guarantees **schema-safe outputs** before responses go to the core backend.

## Orchestrator

`SignalOrchestratorService` calls each extraction service and combines results into one payload. It also computes a combined confidence score by averaging component confidences.

```ts
const confidence = (facts.meta.confidence + intent.meta.confidence + ... ) / 6;
```

## Controllers

Each controller:

1. Validates request body (`text`, optional `provider`, `model`).
2. Builds a service using `ExtractionServiceFactory`.
3. Calls `.extract(...)` or `.classify(...)`.
4. Returns the response.

Example: `SignalsController` maps `POST /extract/signals` to full signal extraction.

## Prompt + Schema Strategy

- Prompts are **strict JSON-only** and explicitly forbid inference.
- Schemas are strict (`.strict()`), rejecting extra fields.
- This reduces hallucinations and enforces reliable downstream normalization.

## Errors + Retries

- `AIClientService` wraps providers with retry logic.
- `AppError` and subclasses unify error responses.
- Zod request validation errors return 400.
- Provider errors return 502.
- Output validation errors return 422.

## Swagger Docs

Swagger is mounted at:
- `/api-docs`
- `/api-docs.json`

The OpenAPI spec is built from controller JSDoc and shared schemas.

## Integration with PDP Core

- Core backend calls this service via `pdp.ai.extraction.base-url`.
- Core’s `ExtractionServiceImpl` routes `/api/extraction/*` to this microservice.
- JWT/auth is handled by core; this service is stateless.

## UI folder

There is a `ui/` folder in this repo, but you said it is not important. Documentation focuses on the service side only.
