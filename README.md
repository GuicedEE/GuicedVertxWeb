# GuicedVertxWeb
Vert.x 5 Web bootstrap module for GuicedEE (Java 25, Maven). Provides SPI hooks to configure the Vert.x Router and HttpServer (HTTP/HTTPS, TLS/keystore, handlers, configurators) via GuiceEE startup.

## Rules Adoption
- Rules submodule: `rules/` (init with `git submodule update --init --recursive`).
- Collaboration pact and stage policy: [PACT.md](PACT.md).
- Project rules and guides: [RULES.md](RULES.md) · [GUIDES.md](GUIDES.md) · [GLOSSARY.md](GLOSSARY.md) · [IMPLEMENTATION.md](IMPLEMENTATION.md) · [docs/PROMPT_REFERENCE.md](docs/PROMPT_REFERENCE.md).
- Architecture diagrams: [docs/architecture/README.md](docs/architecture/README.md) (context, container, component, sequences, ERD).

## Development
- Stack: Java 25 LTS, Maven, Vert.x 5, GuicedEE Core + Client; CRTP fluent APIs and JSpecify nullness.
- Ensure `rules/` is present before prompting; host docs live outside the submodule per Document Modularity and Forward-Only policies.
- Key SPI surfaces (per `module-info.java`): `VertxRouterConfigurator`, `VertxHttpServerConfigurator`, `VertxHttpServerOptionsConfigurator`; startup provider `VertxWebServerPostStartup` implements `IGuicePostStartup`.

## Environment
- Copy `.env.example` to `.env` and adjust:
  - `HTTP_ENABLED` / `HTTP_PORT`
  - `HTTPS_ENABLED` / `HTTPS_PORT`
  - `HTTPS_KEYSTORE` / `HTTPS_KEYSTORE_PASSWORD`
  - `BASE_URL`, `LOG_LEVEL`, `ENABLE_DEBUG_LOGS`, `VERTX_EVENT_LOOP_POOL_SIZE`, `VERTX_WORKER_POOL_SIZE`
- Keep keys aligned with [rules/generative/platform/secrets-config/env-variables.md](rules/generative/platform/secrets-config/env-variables.md).

## CI
- GitHub Actions workflow runs `mvn -B verify` on Java 25 (`.github/workflows/ci.yml`). Configure repository/organization secrets separately; do not commit secrets.
