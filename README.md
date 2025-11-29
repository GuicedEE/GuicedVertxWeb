# GuicedVertxWeb

[![Java](https://img.shields.io/badge/Java-25%20LTS-ED8B00?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![Vert.x](https://img.shields.io/badge/Vert.x-5.0+-662D91?logo=vertx&logoColor=white)](https://vertx.io/)
[![GuicedEE](https://img.shields.io/badge/GuicedEE-Core%20%2B%20Client-4CAF50?logoColor=white)](https://github.com/GuicedEE)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Reactive HTTP/HTTPS server bootstrap for GuicedEE applications using **Vert.x 5**. 

Build high-performance RESTful APIs, WebSocket services, and web applications with dependency injection, automatic lifecycle management, and pluggable SPI configurators. Full support for TLS/HTTPS, static content serving, file uploads, CORS, and authentication.

## ✨ Features

- **🚀 Reactive**: Built on Vert.x 5 for non-blocking, high-throughput request handling
- **💉 Dependency Injection**: Seamless GuicedEE integration with automatic lifecycle management via `IGuicePostStartup`
- **🔌 Extensible SPI**: Three extension points for customizing server options, server instance, and router configuration
- **🔒 Security-First**: Native support for HTTPS/TLS with JKS and PKCS#12 keystores; pluggable authentication/authorization
- **📦 Zero-Config Defaults**: Sensible defaults with environment-based overrides (HTTP, HTTPS, TLS, ports)
- **🎯 Recommended Addons**: Leverage GuicedEE dedicated addons for REST, WebSocket, GraphQL, and web services
- **📊 Full Module System**: JPMS (Java Module System) compliant with automatic SPI discovery via `ServiceLoader`
- **✅ Best Practices**: Built-in patterns for CORS, file uploads, static content, middleware composition, and error handling

## 🎯 Quick Start

### 1. Clone & Initialize

```bash
git clone https://github.com/GuicedEE/GuicedVertxWeb.git
cd GuicedVertxWeb
git submodule update --init --recursive  # Initialize enterprise rules repository
```

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with your settings (ports, HTTPS keystore, debug flags, etc.)
```

### 3. Build & Run

```bash
mvn clean verify
mvn exec:java@run  # or your IDE's run configuration
```

The HTTP server starts on port **8080** (HTTPS on **8443** if enabled). Check logs for confirmation.

## 📚 Documentation

### Project Structure
- **[RULES.md](RULES.md)** — Technology stack, deployment standards, and design patterns
- **[GUIDES.md](GUIDES.md)** — How-to guides for common tasks (REST APIs, WebSockets, HTTPS, etc.)
- **[GLOSSARY.md](GLOSSARY.md)** — Domain terminology with cross-references to enterprise topics
- **[IMPLEMENTATION.md](IMPLEMENTATION.md)** — Current implementation status and validation approach
- **[PACT.md](PACT.md)** — Collaboration agreement and stage approval process
- **[docs/PROMPT_REFERENCE.md](docs/PROMPT_REFERENCE.md)** — Stack traceability and prompt loading instructions

### Architecture & Design
- **[docs/architecture/README.md](docs/architecture/README.md)** — Architecture decision records and diagrams
  - C4 context, container, and component diagrams
  - HTTP request sequence flow
  - Server startup sequence
  - Entity-relationship diagrams (configuration)

### Enterprise Rules
- **[rules/generative/backend/guicedee/web/README.md](rules/generative/backend/guicedee/web/README.md)** — Modular rules index
  - **[spi-configurators.rules.md](rules/generative/backend/guicedee/web/spi-configurators.rules.md)** — SPI interfaces, registration strategies, discovery patterns
  - **[server-configuration.rules.md](rules/generative/backend/guicedee/web/server-configuration.rules.md)** — HTTP/HTTPS setup, environment variables, TLS/keystore configuration
  - **[router-configuration.rules.md](rules/generative/backend/guicedee/web/router-configuration.rules.md)** — Router setup, path patterns, request/response handling, middleware
  - **[use-cases.rules.md](rules/generative/backend/guicedee/web/use-cases.rules.md)** — Practical implementations with recommended GuicedEE addons
  - **[module-info.rules.md](rules/generative/backend/guicedee/web/module-info.rules.md)** — JPMS configuration for consumers
  - **[lifecycle.rules.md](rules/generative/backend/guicedee/web/lifecycle.rules.md)** — Startup/shutdown sequences, SPI discovery, dependency injection ordering
  - **[best-practices.rules.md](rules/generative/backend/guicedee/web/best-practices.rules.md)** — Best practices, troubleshooting, and debugging tips

## 🛠️ Development

### Tech Stack
- **Java 25 LTS** — Latest long-term support release
- **Maven 3.9+** — Build automation and dependency management
- **Vert.x 5** — Reactive, event-driven framework
- **GuicedEE Core + Client** — Dependency injection and lifecycle management
- **JSpecify** — Nullness annotations for static analysis
- **CRTP Fluent APIs** — Type-safe builder patterns without Lombok
- **JPMS** — Java Module System with automatic SPI discovery

### Key SPI Surfaces
Three extension points for customizing the web server:

1. **`VertxHttpServerOptionsConfigurator`** — Customize `HttpServerOptions` before server creation (ports, TLS, compression, etc.)
2. **`VertxHttpServerConfigurator`** — Configure the `HttpServer` instance after creation (WebSocket handlers, metrics, custom bindings)
3. **`VertxRouterConfigurator`** — Add routes to the `Router` (REST endpoints, static content, middleware, error handlers)

Implementations are discovered via `ServiceLoader` and executed in order. Register via JPMS `provides...with` or META-INF/services.

### Startup Hook
- **`VertxWebServerPostStartup`** implements `IGuicePostStartup` and orchestrates the full server initialization sequence
- Automatic lifecycle management: server startup on app startup, graceful shutdown on app exit
- Environment-based configuration via `.env` file or system properties

## ⚙️ Configuration

### Environment Variables
All configuration is driven by `.env` file (or system properties/environment). Copy `.env.example` and customize:

| Variable | Default | Purpose |
|----------|---------|---------|
| `HTTP_ENABLED` | `true` | Enable HTTP server |
| `HTTP_PORT` | `8080` | HTTP listen port |
| `HTTPS_ENABLED` | `false` | Enable HTTPS server |
| `HTTPS_PORT` | `8443` | HTTPS listen port |
| `HTTPS_KEYSTORE` | — | Path to JKS or PKCS#12 keystore file |
| `HTTPS_KEYSTORE_PASSWORD` | — | Keystore password |
| `BASE_URL` | — | Public base URL (for absolute links) |
| `LOG_LEVEL` | `INFO` | Logging level (DEBUG, INFO, WARN, ERROR) |
| `ENABLE_DEBUG_LOGS` | `false` | Verbose Vert.x activity logging |
| `VERTX_EVENT_LOOP_POOL_SIZE` | — | Event loop thread pool size |
| `VERTX_WORKER_POOL_SIZE` | — | Worker thread pool size |

For secrets management, see [rules/generative/platform/secrets-config/env-variables.md](rules/generative/platform/secrets-config/env-variables.md).

### HTTPS/TLS Setup
GuicedVertxWeb auto-detects keystore format by file extension:
- `.jks` → JKS keystore
- `.pfx`, `.p12`, `.p8` → PKCS#12 keystore

Generate a self-signed certificate for development:
```bash
# JKS keystore
keytool -genkey -alias selfsigned -keyalg RSA -keysize 2048 \
  -validity 365 -keystore keystore.jks -storepass changeit

# PKCS#12 keystore
keytool -genkey -alias selfsigned -keyalg RSA -keysize 2048 \
  -validity 365 -keystore keystore.p12 -storetype PKCS12 -storepass changeit
```

## 🚀 Using GuicedEE Addons (Recommended)

For common use cases, GuicedEE provides higher-level addons with automatic features:

- **[guicedee-rest](https://github.com/GuicedEE)** — REST/CRUD APIs with OpenAPI/Swagger documentation, parameter validation, content negotiation, role-based access control
- **[guicedee-websocket](https://github.com/GuicedEE)** — WebSocket connections with lifecycle management, message routing, automatic reconnection
- **[guicedee-webservice](https://github.com/GuicedEE)** — SOAP/XML web services with automatic WSDL generation, MTOM attachments
- **[guicedee-graphql](https://github.com/GuicedEE)** — GraphQL schemas with automatic introspection, query validation, subscriptions
- **GuicedEE Security** — Authorization, authentication, RBAC with declarative annotations

See [use-cases.rules.md](rules/generative/backend/guicedee/web/use-cases.rules.md) for implementation examples and when to use lower-level Vert.x Web APIs directly.

## 🔄 CI/CD

Continuous integration via **GitHub Actions** (`[.github/workflows/ci.yml](.github/workflows/ci.yml)`):

```yaml
name: Maven Package
on:
  workflow_dispatch:
  push:
jobs:
  GuicedInjection:
    uses: GuicedEE/Workflows/.github/workflows/projects.yml@master
    with:
      baseDir: ''
      name: 'Guiced Vert.x Web'
    secrets:
      USERNAME: ${{ secrets.USERNAME }}
      USER_TOKEN: ${{ secrets.USER_TOKEN }}
      SONA_USERNAME: ${{ secrets.SONA_USERNAME }}
      SONA_PASSWORD: ${{ secrets.SONA_PASSWORD }}
```

**Required repository secrets:**
- `USERNAME` — GitHub username for deployments
- `USER_TOKEN` — GitHub personal access token
- `SONA_USERNAME` — Sonatype (Maven Central) username
- `SONA_PASSWORD` — Sonatype password

Do **not** commit secrets. Configure via GitHub repository settings → Secrets and variables → Actions.

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork & Branch**: Create a feature branch (`git checkout -b feature/my-feature`)
2. **Follow the Pact**: Review [PACT.md](PACT.md) for collaboration standards and approval stages
3. **Run Tests**: `mvn clean verify` (Java 25 required)
4. **Update Rules**: If behavior changes, update relevant `.rules.md` files in `rules/generative/backend/guicedee/web/`
5. **Commit & Push**: Descriptive commit messages, push to your fork
6. **Pull Request**: Open a PR with clear description and reference relevant issues

### Development Workflow
```bash
# Build and test locally
mvn clean install

# Run the application
mvn exec:java@run

# Format code
mvn spotless:apply

# Run only tests
mvn test
```

## 📋 Project Status

- **Current Version**: See `pom.xml` for latest release
- **Java Compatibility**: Java 25 LTS minimum
- **Maven Compatibility**: Maven 3.9+
- **Vert.x Compatibility**: Vert.x 5.0+
- **Status**: Active development with enterprise rules maintained in submodule

## 📜 License

This project is licensed under the **Apache License 2.0**. See [LICENSE](LICENSE) file for details.
