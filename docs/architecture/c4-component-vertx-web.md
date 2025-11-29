# C4 Level 3 — Vert.x Web Components

Component breakdown derived from `module-info.java` SPIs and expected startup path.

```mermaid
graph TD
    VertxWebServerPostStartup["VertxWebServerPostStartup\n(IGuicePostStartup)"] --> ServiceLoader["ServiceLoader of SPI interfaces"]
    ServiceLoader --> RouterConfig["VertxRouterConfigurator implementations"]
    ServiceLoader --> HttpConfig["VertxHttpServerConfigurator implementations"]
    ServiceLoader --> HttpOptions["VertxHttpServerOptionsConfigurator implementations"]

    VertxWebServerPostStartup --> VertxCore["Vert.x Core / HttpServer"]
    VertxWebServerPostStartup --> Router["Vert.x Router"]

    RouterConfig --> Router
    HttpConfig --> VertxCore
    HttpOptions --> VertxCore

    Router --> Handlers["Route Handlers (application-provided)"]
```

Observations and assumptions:
- JPMS `uses` entries indicate configurators are discovered dynamically; implementers live in host modules.
- The module exports only `com.guicedee.vertx.web.spi`, keeping server setup classes internal.
- `VertxWebServerPostStartup` reads `HTTP_ENABLED`/`HTTP_PORT` and `HTTPS_ENABLED`/`HTTPS_PORT` plus `HTTPS_KEYSTORE`/`HTTPS_KEYSTORE_PASSWORD` to create servers and configure TLS.
- A default `BodyHandler` is registered (uploads → `uploads/`, deleted on request end); override via router configurators as needed.
