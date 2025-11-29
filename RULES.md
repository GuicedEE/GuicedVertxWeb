# Project RULES — GuicedVertxWeb

Scope: Java 25 LTS, Maven-built Vert.x 5 Web bootstrap module for GuicedEE (Core + Client). Apply CRTP for fluent APIs, JSpecify for nullness, and GitHub Actions for CI. Host docs stay outside the `rules/` submodule.

## Anchors

- PACT: `PACT.md` (collaboration + stage policy — blanket approval noted).
- Glossary: `GLOSSARY.md` (topic-first precedence).
- Architecture: `docs/architecture/README.md` (C4/sequence/ERD).
- Enterprise rules: `rules/RULES.md` — respect sections 4, 5, Document Modularity, and 6 (Forward-Only).

## Stack Selections

- Language/Build: Java 25 LTS, Maven (`pom.xml`), JPMS module `com.guicedee.vertx.web`.
- Platform: Vert.x 5, GuicedEE Core + Client.
- Fluent API: CRTP (no Lombok builders); Lombok may be present but avoid `@Builder`.
- Typing: JSpecify for nullness defaults.
- CI/CD: GitHub Actions.
- Secrets/Env: `.env.example` aligned to secrets-config rules.

## Rule Sources (link-first, minimal duplication)

- Java: [rules/generative/language/java/README.md](rules/generative/language/java/README.md), [rules/generative/language/java/java-25.rules.md](rules/generative/language/java/java-25.rules.md), [rules/generative/language/java/build-tooling.md](rules/generative/language/java/build-tooling.md)
- Fluent API: [rules/generative/backend/fluent-api/crtp.rules.md](rules/generative/backend/fluent-api/crtp.rules.md)
- Nullness: [rules/generative/backend/jspecify/README.md](rules/generative/backend/jspecify/README.md)
- GuicedEE platform: [rules/generative/backend/guicedee/README.md](rules/generative/backend/guicedee/README.md), [rules/generative/backend/guicedee/web/README.md](rules/generative/backend/guicedee/web/README.md), [rules/generative/backend/guicedee/vertx/README.md](rules/generative/backend/guicedee/vertx/README.md), [rules/generative/backend/vertx/README.md](rules/generative/backend/vertx/README.md)
- GuicedEE Vert.x Web (core rules): [rules/generative/backend/guicedee/web/guiced-vertx-web-rules.md](rules/generative/backend/guicedee/web/guiced-vertx-web-rules.md), [rules/generative/backend/guicedee/web/GLOSSARY.md](rules/generative/backend/guicedee/web/GLOSSARY.md)
- GuicedEE functions (general): [rules/generative/backend/guicedee/functions/guiced-injection-rules.md](rules/generative/backend/guicedee/functions/guiced-injection-rules.md), [rules/generative/backend/guicedee/functions/guiced-vertx-rules.md](rules/generative/backend/guicedee/functions/guiced-vertx-rules.md)
- CI/CD: [rules/generative/platform/ci-cd/README.md](rules/generative/platform/ci-cd/README.md), [rules/generative/platform/ci-cd/providers/github-actions.md](rules/generative/platform/ci-cd/providers/github-actions.md)
- Secrets/Env: [rules/generative/platform/secrets-config/env-variables.md](rules/generative/platform/secrets-config/env-variables.md)
- Observability/Logging: [rules/generative/platform/observability/README.md](rules/generative/platform/observability/README.md) (for logging/metrics alignment)

## Conventions

- **CRTP fluent APIs:** Fluent setters return `(J)this`; avoid Lombok `@Builder`. Keep JPMS exports minimal; prefer package-private for internals.
- **SPI naming:** Keep `Vertx*Configurator` names aligned to `module-info.java`. Register providers via `uses/provides` consistent with JPMS and GuiceEE expectations.
- **Nullness:** Apply JSpecify annotations to public APIs; default non-null unless specified.
- **Docs modularity:** Replace monolithic docs with links to modular rules/guides; update indexes alongside changes.
- **Forward-only:** No legacy anchors/shims. Update all references (README, GUIDES, IMPLEMENTATION) when altering APIs or docs.
- **CI/Env:** Prefer GitHub Actions templates from rules, secrets via `.env.example`, and avoid embedding secrets in code or workflows.
