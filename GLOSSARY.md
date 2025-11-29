# Glossary (Topic-First)

This glossary indexes topic glossaries from the Rules Repository and adds only project-specific terms for `guiced-vertx-web`. Topic glossaries override host definitions within their scope.

## Topic Glossaries (precedence order)

- [rules/generative/backend/fluent-api/GLOSSARY.md](rules/generative/backend/fluent-api/GLOSSARY.md) — CRTP fluent API conventions (selected strategy; no Lombok builders).
- [rules/generative/backend/guicedee/GLOSSARY.md](rules/generative/backend/guicedee/GLOSSARY.md) — GuicedEE platform terminology and injection model.
- [rules/generative/backend/guicedee/vertx/GLOSSARY.md](rules/generative/backend/guicedee/vertx/GLOSSARY.md) — Vert.x integration vocabulary under GuicedEE.
- [rules/generative/backend/guicedee/client/GLOSSARY.md](rules/generative/backend/guicedee/client/GLOSSARY.md) — Client/bootstrap lifecycle terms.
- [rules/generative/backend/jspecify/GLOSSARY.md](rules/generative/backend/jspecify/GLOSSARY.md) — Nullness defaults and annotations.
- [rules/generative/language/java/GLOSSARY.md](rules/generative/language/java/GLOSSARY.md) — Java 25/JVM terminology.
- [rules/GLOSSARY.md](rules/GLOSSARY.md) — General/shared terms when no topic-specific entry exists.

If a term appears in multiple glossaries, prefer the most specific topic first (e.g., GuicedEE Vert.x over generic GuicedEE, then root).

## Prompt Language Alignment (enforced excerpts)

- **CRTP fluent setters:** Return `(J)this` from fluent methods; avoid Lombok `@Builder`.
- **JSpecify defaults:** Apply nullness annotations per jspecify glossary; avoid unchecked assumptions.

## Project-Specific Terms

- **VertxWebServerPostStartup:** `IGuicePostStartup` provider registered in `module-info.java`; builds Vert.x HttpServer/Router using configurators.
- **VertxRouterConfigurator:** SPI interface (JPMS `uses`) that registers routes/handlers on the Vert.x Router.
- **VertxHttpServerConfigurator:** SPI interface that tunes HttpServer behavior (ports, TLS hooks) before start.
- **VertxHttpServerOptionsConfigurator:** SPI interface that adjusts `HttpServerOptions` (protocols, compression, ALPN).
- **SPI Implementor Module:** Any host module that implements the configurator interfaces and is discovered at runtime.
- **HTTP_ENABLED / HTTP_PORT / HTTPS_ENABLED / HTTPS_PORT / HTTPS_KEYSTORE / HTTPS_KEYSTORE_PASSWORD:** Env flags consumed by `VertxWebServerPostStartup` to create HTTP/HTTPS servers and configure TLS keystore source.

## Glossary Precedence Policy

1. Use topic glossaries above before defining host terms.
2. Only add project-specific entries when no topic glossary covers them.
3. Keep host entries brief and link back to topic glossaries for authoritative definitions.
