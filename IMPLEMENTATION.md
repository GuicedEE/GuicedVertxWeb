# IMPLEMENTATION — Current State & Plan

This document ties the observed code to guides and rules. It will evolve as Stage 4 changes land.

## Current State (evidence-based)

- Module: `src/main/java/module-info.java` declares `module com.guicedee.vertx.web`:
  - `uses` `VertxRouterConfigurator`, `VertxHttpServerConfigurator`, `VertxHttpServerOptionsConfigurator`.
  - `exports` `com.guicedee.vertx.web.spi`.
  - `provides` `IGuicePostStartup` with `VertxWebServerPostStartup`.
  - `requires transitive` `com.guicedee.vertx`, `io.vertx.web`, `io.vertx.core`; `requires static lombok`.
- Build: Maven `pom.xml` with GuicedEE parents/BOMs, Vert.x dependencies, Lombok and JUnit.
- Docs/Rules: `rules/` submodule present via `.gitmodules`; host docs now include `PACT.md`, `GLOSSARY.md`, `RULES.md`, `GUIDES.md`, `docs/architecture/*`, `docs/PROMPT_REFERENCE.md`.

## Planned Scaffolding (Stage 4)

- **README alignment:** Declare Rules submodule usage, link PACT/RULES/GLOSSARY/GUIDES/IMPLEMENTATION/architecture index, and summarize stack selection (Java 25, Vert.x 5, GuicedEE).
- **Env:** Add `.env.example` with `HTTP_ENABLED`/`HTTP_PORT`, `HTTPS_ENABLED`/`HTTPS_PORT`, `HTTPS_KEYSTORE`/`HTTPS_KEYSTORE_PASSWORD`, log level, and Vert.x worker/event loop sizing per env-variables rules.
- **CI:** Add GitHub Actions workflow running `mvn -B verify` on Java 25 with JPMS/module cache, and document secrets (if any) in README.
- **Traceability:** Ensure new files link back to rules (env vars → secrets-config; CI → ci-cd/providers/github-actions) and diagrams.
- **No code changes planned** in Stage 4 unless wiring is required for docs/CI/env; any future code additions must follow CRTP + JSpecify + JPMS constraints.

## Rollout Plan

1. Add env + CI scaffolding and README updates in a single forward-only change set.
2. Validate links and glossary references across PACT/RULES/GUIDES/IMPLEMENTATION/architecture.
3. Run Maven verify on Java 25; fix module/service descriptor issues if they surface.
4. Document any risky removals or changes in `MIGRATION.md` if future steps require deleting legacy docs.

## Module/File Tree (target after Stage 4)

- `README.md` — project overview + Rules adoption + links.
- `.env.example` — placeholder envs for router/server/TLS/logging.
- `.github/workflows/ci.yml` — Maven verify on pushes/PRs.
- `PACT.md`, `RULES.md`, `GUIDES.md`, `GLOSSARY.md`, `IMPLEMENTATION.md`, `docs/architecture/*`, `docs/PROMPT_REFERENCE.md` — docs loop.
- `rules/` — submodule (no host docs inside).

## Validation Approach

- Build: `mvn -B verify` on Java 25; ensure module-info aligns with available classes from dependencies or source.
- Docs: Check all links resolve and point to modular rule files; glossary precedence respected.
- Env: `.env.example` variables referenced consistently in README/GUIDES; no secrets committed.
- CI: Workflow uses Java 25 toolchain and caches Maven to speed builds.

## Risks / Unknowns

- SPI implementations are not present in the repo; signatures should be confirmed before adding handlers.
- TLS/keystore expectations are implied in description but absent in code; env placeholders may need adjustment once concrete configuration APIs are inspected.
- Lombok is declared but CRTP strategy discourages builder usage; ensure downstream modules do not rely on Lombok builders for SPIs.
