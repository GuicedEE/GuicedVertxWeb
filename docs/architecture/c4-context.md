# C4 Level 1 — System Context

`guiced-vertx-web` exposes a JPMS module (`com.guicedee.vertx.web`) that boots a Vert.x Web server and router via GuicedEE SPIs. The context reflects what is visible from `module-info.java` and `pom.xml`.

```mermaid
graph TD
    Dev[Developers/Ops] -->|configure SPI + env| VertxWeb[Guiced Vert.x Web Module]
    App[GuicedEE Host Application] -->|depends on| VertxWeb
    VertxWeb -->|bootstraps| VertxRuntime[Vert.x 5 Runtime]
    VertxWeb -->|integrates via JPMS| GuiceEE[GuicedEE Injector/Core]
    VertxWeb -->|exposes SPI for| Configurators[Router/Server Configurators]
    Configurators -->|implemented by| App
    HttpClient[HTTP Clients] -->|send requests| VertxRuntime
    Secrets[Keystore/TLS/.env] -->|read at startup| VertxWeb
```

Trust boundaries: HTTP clients interact over the network with Vert.x; configurators are application-provided; secrets flow in through keystore/env during server bootstrap.
