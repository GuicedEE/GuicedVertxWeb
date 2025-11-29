# C4 Level 2 — Containers

Container responsibilities inferred from `module-info.java` (SPI usage/provision) and Maven dependencies.

```mermaid
graph TD
    subgraph Host["GuicedEE Host Application"]
        AppModule[Application Modules
        SPI implementations
        handlers]
        GuiceInjector[GuiceEE Injector]
    end

    subgraph VertxWeb["Guiced Vert.x Web Module (com.guicedee.vertx.web)"]
        Startup[VertxWebServerPostStartup
        IGuicePostStartup provider]
        SPIs[VertxRouterConfigurator
        VertxHttpServerConfigurator
        VertxHttpServerOptionsConfigurator]
    end

    subgraph Vertx["Vert.x Runtime"]
        HttpServer[Vert.x HttpServer]
        Router[Vert.x Router]
    end

    AppModule -->|implements| SPIs
    GuiceInjector -->|invokes| Startup
    Startup -->|loads| SPIs
    Startup -->|creates| HttpServer
    Startup -->|wires| Router
    HttpServer -->|routes to| Router
    HttpClient[HTTP Clients] --> HttpServer
    Secrets[Keystore/TLS/.env] --> Startup
```

Notes:
- JPMS exports `com.guicedee.vertx.web.spi` for configurator types.
- Startup provider is registered through `provides IGuicePostStartup with VertxWebServerPostStartup`.
