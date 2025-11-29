# Architecture Index

This directory captures the current understanding of `guiced-vertx-web` based on the observed JPMS module descriptor and Maven configuration. Diagrams follow the C4 model and sequence/ERD views, and will evolve as code is extended.

- [System Context](c4-context.md)
- [Container View](c4-container.md)
- [Vert.x Web Component View](c4-component-vertx-web.md)
- [Startup Sequence](sequence-startup.md)
- [HTTP Request Sequence](sequence-http-request.md)
- [Core Configuration ERD](erd-core.md)

Threat/Trust notes:
- Trust boundary between external HTTP clients and Vert.x HttpServer; keystore/env are sensitive inputs during startup.
- SPI implementors run inside the same process; treat them as trusted application code but validate their options before wiring.
- Dependencies: Vert.x core/web, GuicedEE injector, and application-provided configurator modules; no external services observed.

Sources: `pom.xml`, `src/main/java/module-info.java`, `src/main/java/com/guicedee/vertx/web/VertxWebServerPostStartup.java`, and SPI interfaces under `src/main/java/com/guicedee/vertx/web/spi/`.
