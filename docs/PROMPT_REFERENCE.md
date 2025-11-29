# Prompt Reference

Load this file before running future prompts to align with the Rules Repository and host project constraints.

- **Project:** GuicedEE / GuicedVertxWeb (`guiced-vertx-web`)
- **Stacks:** Java 25 LTS, Maven; Vert.x 5; GuicedEE Core + Client; Fluent API strategy = CRTP (no Lombok builders); Logging; JSpecify; CI = GitHub Actions.
- **Rules anchors:** Follow `rules/RULES.md` sections 4, 5, Document Modularity Policy, and 6 (Forward-Only). Keep host docs outside the `rules/` submodule.
- **Glossary precedence:** Topic-first; use `GLOSSARY.md` as the index and defer to topic glossaries (GuicedEE, Vert.x, CRTP, Java, JSpecify) for definitions and naming.
- **Diagrams (docs/architecture/):** [Context](architecture/c4-context.md) · [Container](architecture/c4-container.md) · [Component](architecture/c4-component-vertx-web.md) · [Startup](architecture/sequence-startup.md) · [HTTP Request](architecture/sequence-http-request.md) · [ERD](architecture/erd-core.md) · [Index](architecture/README.md)
- **Env keys:** Use `.env.example` as the source of truth for `HTTP_ENABLED`/`HTTP_PORT`, `HTTPS_ENABLED`/`HTTPS_PORT`, `HTTPS_KEYSTORE`/`HTTPS_KEYSTORE_PASSWORD`, `BASE_URL`, logging, and Vert.x pool sizes; align with `rules/generative/platform/secrets-config/env-variables.md`.
- **Traceability:** Close loops PACT ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION; reference architecture diagrams when changing SPI behaviors or Vert.x wiring.
- **Approval policy:** Blanket approval recorded in `PACT.md` for this run; still document stage boundaries in responses.
