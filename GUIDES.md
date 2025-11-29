# GUIDES — Applying the Rules to GuicedVertxWeb

These guides explain how to apply the selected rules to the existing Vert.x Web bootstrap module. Follow `GLOSSARY.md` for naming and topic precedence.

## SPI Usage (API Surface Sketch)

- Exported SPI package: `com.guicedee.vertx.web.spi` (per `module-info.java`).
- Service hooks discovered via JPMS `uses`:
  - `Router builder(Router router)` (VertxRouterConfigurator)
  - `HttpServer builder(HttpServer server)` (VertxHttpServerConfigurator)
  - `HttpServerOptions builder(HttpServerOptions options)` (VertxHttpServerOptionsConfigurator)
- Startup hook: `VertxWebServerPostStartup` provides `IGuicePostStartup`.
- Application pattern:
  1. Implement configurator interfaces in host modules; keep JPMS `module-info` updated with `provides` entries.
  2. Use options configurators to set ports/TLS/ALPN/compression; server configurators to attach shared handlers; router configurators to register routes.
  3. Keep configurators idempotent; avoid side effects beyond router/server setup.
  4. Leverage GuiceEE injector for dependency injection inside configurators when available.
  5. Default BodyHandler is applied with uploads stored under `uploads/` and cleaned after requests; override via router configurator if needed.

Reference rules: [guiced-vertx-web-rules](rules/generative/backend/guicedee/web/guiced-vertx-web-rules.md), [guiced-vertx-rules](rules/generative/backend/guicedee/functions/guiced-vertx-rules.md), [guiced-injection-rules](rules/generative/backend/guicedee/functions/guiced-injection-rules.md).

## Fluent API & Nullness

- Adopt CRTP per [crtp.rules.md](rules/generative/backend/fluent-api/crtp.rules.md); avoid Lombok `@Builder`.
- Apply JSpecify annotations on public APIs and SPI implementations per [jspecify README](rules/generative/backend/jspecify/README.md).

## Env & Secrets

- Mirror env variables in `.env.example` following [env-variables.md](rules/generative/platform/secrets-config/env-variables.md).
- Server toggles and ports: `HTTP_ENABLED`, `HTTP_PORT`, `HTTPS_ENABLED`, `HTTPS_PORT`.
- TLS: `HTTPS_KEYSTORE`, `HTTPS_KEYSTORE_PASSWORD` (expects JKS/PFX/P12 based on file extension).
- Runtime: `BASE_URL`, `LOG_LEVEL`, `ENABLE_DEBUG_LOGS`, `VERTX_EVENT_LOOP_POOL_SIZE`, `VERTX_WORKER_POOL_SIZE`.
- Do not commit secrets; rely on GitHub Actions/repo secrets for CI.

## CI / Build

- Use Maven with Java 25 toolchain settings from [java-25.rules.md](rules/generative/language/java/java-25.rules.md) and [build-tooling.md](rules/generative/language/java/build-tooling.md).
- Base CI on [GitHub Actions provider guide](rules/generative/platform/ci-cd/providers/github-actions.md); run `mvn -B verify` and JPMS module checks.

## Validation & Testing

- Startup validation: ensure `IGuicePostStartup` fires and the HttpServer listens on configured endpoints (HTTP/HTTPS as applicable).
- Routing validation: confirm configurators register handlers and respect order; include tests or probes to assert handler chains.
- Nullness validation: JSpecify linting/static analysis where available; avoid unchecked casts in CRTP chains.
- CI acceptance: GitHub Actions succeeds on `mvn -B verify`; JPMS module graph intact; no missing `uses/provides` registrations for configurators.

## Acceptance Criteria (Stage 2)

- PACT/RULES/GLOSSARY/GUIDES/IMPLEMENTATION cross-link to diagrams and rules.
- Documented SPI usage aligns with `module-info.java` exports/uses/provides.
- Build/test guidance targets Java 25 + Maven with GuicedEE/Vert.x dependencies.
- Env and CI guidance reference rules submodule paths instead of duplicating instructions.

## Migration/Assumptions

- Existing README and code are minimal; SPI method signatures should be re-confirmed in source before adding handlers.
- Logging and observability follow [observability README](rules/generative/platform/observability/README.md); add structured logs around startup and routing in later implementation steps.
