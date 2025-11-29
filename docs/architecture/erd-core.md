# ERD — Core Configuration Relationships

Conceptual relationships for Vert.x Web server configuration objects implied by the JPMS SPI contracts. No persistent datastore is defined in the repository; this ERD captures configuration ownership for traceability.

```mermaid
erDiagram
    VertxWebServer ||--o{ RouterConfigurator : applies
    VertxWebServer ||--o{ HttpServerConfigurator : shapes
    VertxWebServer ||--o{ HttpServerOptionsConfigurator : tunes
    RouterConfigurator ||--o{ RouteHandler : registers
    HttpServerConfigurator ||--|| HttpServerOptions : consumes

    VertxWebServer {
        string moduleName
        string listenAddress
        int listenPort
        boolean tlsEnabled
    }
    RouterConfigurator {
        string name
        string orderHint
    }
    HttpServerConfigurator {
        string name
        string phase
    }
    HttpServerOptions {
        string protocol
        string tlsMode
        string compression
    }
    RouteHandler {
        string path
        string httpMethod
    }
```

Assumptions are placeholders until concrete configurator implementations and configuration schemas are present in the codebase.
