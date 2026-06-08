package com.guicedee.vertx.web;

import com.google.inject.Inject;
import com.guicedee.client.Environment;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import com.guicedee.client.scopes.CallScoper;
import com.guicedee.client.scopes.CallScopeProperties;
import com.guicedee.client.scopes.CallScopeSource;
import com.guicedee.modules.services.jsonrepresentation.IJsonRepresentation;
import com.guicedee.vertx.spi.VertXPreStartup;
import com.guicedee.vertx.web.spi.VertxHttpServerConfigurator;
import com.guicedee.vertx.web.spi.VertxHttpServerOptionsConfigurator;
import com.guicedee.vertx.web.spi.VertxRouterConfigurator;
import com.guicedee.vertx.spi.VerticleBuilder;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Bootstraps Vert.x HTTP/HTTPS servers and router configuration after the Guice context
 * has completed startup.
 *
 * <p>This initializer builds default {@link io.vertx.core.http.HttpServerOptions}, allows
 * SPI-based customization via {@link com.guicedee.vertx.web.spi.VertxHttpServerOptionsConfigurator},
 * creates HTTP/HTTPS servers based on environment settings, wires router handlers via
 * {@link com.guicedee.vertx.web.spi.VertxRouterConfigurator}, and finally starts all servers.</p>
 *
 * <p>Configuration is driven by system properties or environment variables:</p>
 * <ul>
 *   <li>HTTP_ENABLED (default true)</li>
 *   <li>HTTP_PORT (default 8080)</li>
 *   <li>HTTPS_ENABLED (default false)</li>
 *   <li>HTTPS_PORT (default 443)</li>
 *   <li>HTTPS_KEYSTORE</li>
 *   <li>HTTPS_KEYSTORE_TYPE</li>
 *   <li>HTTPS_KEYSTORE_PASSWORD</li>
 *   <li>HTTP_HOST</li>
 *   <li>HTTP_COMPRESSION_ENABLED (default true)</li>
 *   <li>HTTP_COMPRESSION_LEVEL (default 9)</li>
 *   <li>HTTP_DECOMPRESSION_ENABLED (default false)</li>
 *   <li>HTTP_TCP_KEEP_ALIVE (default true)</li>
 *   <li>HTTP_IDLE_TIMEOUT (default 0 = disabled)</li>
 *   <li>HTTP_MAX_HEADER_SIZE (default 65536)</li>
 *   <li>HTTP_MAX_CHUNK_SIZE (default 65536)</li>
 *   <li>HTTP_MAX_FORM_ATTRIBUTE_SIZE (default 65536)</li>
 *   <li>HTTP_MAX_FORM_FIELDS (default -1 = unlimited)</li>
 *   <li>HTTP_MAX_INITIAL_LINE_LENGTH (default 65536)</li>
 *   <li>VERTX_MAX_BODY_SIZE</li>
 *   <li>HTTP_UPLOADS_DIRECTORY (default uploads)</li>
 *   <li>HTTP_DELETE_UPLOADS_ON_END (default true)</li>
 *   <li>HTTP_HANDLE_FILE_UPLOADS (default true)</li>
 * </ul>
 */
@Log4j2
public class VertxWebServerPostStartup implements IGuicePostStartup<VertxWebServerPostStartup> {

    /**
     * Initializes server options, creates and configures HTTP/HTTPS servers, builds the router,
     * and starts listening asynchronously.
     *
     * @return a list containing a single {@link Future} that completes once startup is scheduled.
     */
    @Override
    public List<Uni<Boolean>> postLoad() {
        var vertx = VertXPreStartup.getVertx();
        log.debug("🚀 Starting Vertx Web Server initialization");
        log.trace("📋 Creating server options with default configuration");
        return List.of(Uni.createFrom().item(() -> {
                    HttpServerOptions serverOptions = new HttpServerOptions();
                    serverOptions.setCompressionSupported(Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("HTTP_COMPRESSION_ENABLED", "true")));
                    serverOptions.setCompressionLevel(Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_COMPRESSION_LEVEL", "9")));
                    serverOptions.setTcpKeepAlive(Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("HTTP_TCP_KEEP_ALIVE", "true")));
                    serverOptions.setMaxHeaderSize(Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_MAX_HEADER_SIZE", "65536")));
                    serverOptions.setMaxChunkSize(Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_MAX_CHUNK_SIZE", "65536")));
                    serverOptions.setMaxFormAttributeSize(Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_MAX_FORM_ATTRIBUTE_SIZE", "65536")));
                    serverOptions.setMaxFormFields(Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_MAX_FORM_FIELDS", "-1")));
                    serverOptions.setMaxInitialLineLength(Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_MAX_INITIAL_LINE_LENGTH", "65536")));

                    int idleTimeout = Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_IDLE_TIMEOUT", "0"));
                    if (idleTimeout > 0) {
                        serverOptions.setIdleTimeout(idleTimeout);
                    }

                    String host = Environment.getSystemPropertyOrEnvironment("HTTP_HOST", "");
                    if (!host.isEmpty()) {
                        serverOptions.setHost(host);
                    }

                    serverOptions.setDecompressionSupported(Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("HTTP_DECOMPRESSION_ENABLED", "false")));

                    log.debug("📋 Default server options configured - Compression: {}(level {}), TCP KeepAlive: {}, MaxHeaderSize: {} bytes, MaxInitialLineLength: {} bytes",
                            serverOptions.isCompressionSupported(), serverOptions.getCompressionLevel(),
                            serverOptions.isTcpKeepAlive(), serverOptions.getMaxHeaderSize(), serverOptions.getMaxInitialLineLength());

                    log.trace("🔍 Loading VertxHttpServerOptionsConfigurator services");
                    ServiceLoader<VertxHttpServerOptionsConfigurator> options = ServiceLoader.load(VertxHttpServerOptionsConfigurator.class);
                    for (VertxHttpServerOptionsConfigurator option : options) {
                        log.trace("📋 Applying server options from configurator: {}", option.getClass().getName());
                        serverOptions = option.builder(IGuiceContext.get(serverOptions.getClass()));
                    }

                    log.trace("📋 Preparing HTTP/HTTPS server creation");
                    List<HttpServer> httpServers = new ArrayList<>();

                    // HTTP Server setup
                    boolean httpEnabled = Boolean.parseBoolean(Environment.getProperty("HTTP_ENABLED", "true"));
                    if (httpEnabled) {
                        int httpPort = Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_PORT", "8080"));
                        log.trace("🚀 Creating HTTP server on port: {}", httpPort);
                        serverOptions.setPort(httpPort);
                        var server = vertx.createHttpServer(serverOptions);
                        httpServers.add(server);
                        log.debug("✅ HTTP server created successfully");
                    } else {
                        log.warn("📋 HTTP server disabled by configuration");
                    }

                    // HTTPS Server setup
                    boolean httpsEnabled = Boolean.parseBoolean(Environment.getProperty("HTTPS_ENABLED", "false"));
                    if (httpsEnabled) {
                        int httpsPort = Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTPS_PORT", "443"));
                        log.trace("🚀 Creating HTTPS server on port: {}", httpsPort);

                        // Configure SSL
                        serverOptions.setSsl(true).setUseAlpn(true);

                        // IMPORTANT: Vert.x core has no RFC 8441 support (WebSocket over HTTP/2).
                        // The default ALPN list advertises HTTP/2 first ([HTTP_2, HTTP_1_1]), so browsers
                        // negotiate h2 on wss:// connections. The HTTP/1.1 "Upgrade: websocket" handshake
                        // then has nothing to negotiate against and fails with a bare
                        // "WebSocket connection to 'wss://...' failed" (plain HTTP works because it never
                        // negotiates ALPN and stays HTTP/1.1).
                        // Restrict the TLS server to HTTP/1.1 by default so WebSocket/STOMP upgrades work
                        // over wss://. Set HTTP2_ENABLED=true to advertise HTTP/2 as well (only safe if the
                        // application does not rely on server-side WebSockets).
                        boolean http2Enabled = Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("HTTP2_ENABLED", "false"));
                        if (http2Enabled) {
                            log.debug("📋 HTTPS ALPN advertising HTTP/2 and HTTP/1.1 (HTTP2_ENABLED=true) - server WebSockets over wss:// may fail");
                            serverOptions.setAlpnVersions(List.of(HttpVersion.HTTP_2, HttpVersion.HTTP_1_1));
                        } else {
                            log.debug("📋 HTTPS ALPN restricted to HTTP/1.1 to keep wss:// WebSocket upgrades working (set HTTP2_ENABLED=true to override)");
                            serverOptions.setAlpnVersions(List.of(HttpVersion.HTTP_1_1));
                        }

                        serverOptions.setPort(httpsPort);

                        String keystorePath = Environment.getSystemPropertyOrEnvironment("HTTPS_KEYSTORE", "");
                        String keystoreType = Environment.getSystemPropertyOrEnvironment("HTTPS_KEYSTORE_TYPE", "").trim().toUpperCase();
                        log.debug("📋 Using keystore: {} (type override: {})", keystorePath, keystoreType.isEmpty() ? "auto-detect" : keystoreType);

                        boolean isPfx = "PKCS12".equals(keystoreType) || "PFX".equals(keystoreType)
                                || (keystoreType.isEmpty() && (keystorePath.toLowerCase().endsWith("pfx") ||
                                keystorePath.toLowerCase().endsWith("p12") ||
                                keystorePath.toLowerCase().endsWith("p8")));
                        boolean isJks = "JKS".equals(keystoreType)
                                || (keystoreType.isEmpty() && keystorePath.toLowerCase().endsWith("jks"));

                        if (isPfx) {
                            log.trace("🔐 Configuring PFX/PKCS12 keystore");
                            String keystorePassword = Environment.getSystemPropertyOrEnvironment("HTTPS_KEYSTORE_PASSWORD", "");
                            serverOptions.setKeyCertOptions(new PfxOptions()
                                    .setPassword(keystorePassword)
                                    .setPath(keystorePath));
                        } else if (isJks) {
                            log.trace("🔐 Configuring JKS keystore");
                            String keystorePassword = Environment.getSystemPropertyOrEnvironment("HTTPS_KEYSTORE_PASSWORD", "changeit");
                            serverOptions.setKeyCertOptions(new JksOptions()
                                    .setPassword(keystorePassword)
                                    .setPath(keystorePath));
                        } else {
                            log.warn("⚠️ No valid keystore format detected for path: {}", keystorePath);
                        }

                        var server = vertx.createHttpServer(serverOptions);
                        httpServers.add(server);
                        log.trace("✅ HTTPS server created successfully");
                    } else {
                        log.warn("📋 HTTPS server disabled by configuration");
                    }

                    log.debug("📊 Server summary: HTTP enabled: {}, HTTPS enabled: {}, Total servers: {}",
                            httpEnabled, httpsEnabled, httpServers.size());

                    log.trace("🔍 Loading VertxHttpServerConfigurator services");
                    ServiceLoader<VertxHttpServerConfigurator> servers = ServiceLoader.load(VertxHttpServerConfigurator.class);
                    int serverConfigCount = 0;
                    for (VertxHttpServerConfigurator configurator : servers) {
                        serverConfigCount++;
                        log.debug("📋 Applying server configurator: {}", configurator.getClass().getName());
                        for (var server : httpServers) {
                            IGuiceContext.get(configurator.getClass()).builder(server);
                        }
                    }
                    log.debug("✅ Applied {} server configurators to {} servers", serverConfigCount, httpServers.size());

                    log.trace("🔗 Creating and configuring Vertx Router");
                    Router router = Router.router(vertx);



                    long maxBodySize = Long.parseLong(Environment.getSystemPropertyOrEnvironment("VERTX_MAX_BODY_SIZE", String.valueOf(500L * 1024 * 1024)));
                    String uploadsDir = Environment.getSystemPropertyOrEnvironment("HTTP_UPLOADS_DIRECTORY", "uploads");
                    boolean deleteUploads = Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("HTTP_DELETE_UPLOADS_ON_END", "true"));
                    boolean handleFileUploads = Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("HTTP_HANDLE_FILE_UPLOADS", "true"));
                    log.trace("📋 Setting up BodyHandler with uploads directory: '{}', deleteUploadedFilesOnEnd: {}, bodyLimit: {} bytes, handleFileUploads: {}", uploadsDir, deleteUploads, maxBodySize, handleFileUploads);
                    router.route().handler(BodyHandler.create()
                            .setUploadsDirectory(uploadsDir)
                            .setDeleteUploadedFilesOnEnd(deleteUploads)
                            .setHandleFileUploads(handleFileUploads)
                            .setBodyLimit(maxBodySize)
                            .setMergeFormAttributes(true));

                    log.trace("🔍 Loading VertxRouterConfigurator services");
                    ServiceLoader<VertxRouterConfigurator> routes = ServiceLoader.load(VertxRouterConfigurator.class);
                    List<VertxRouterConfigurator> sortedRoutes = new ArrayList<>();
                    routes.forEach(sortedRoutes::add);
                    sortedRoutes.sort(VertxRouterConfigurator::compareTo);
                    
                    // Filter out configurators that are already handled by per-package verticles
                    var excludedPrefixes = VerticleBuilder.getAnnotatedPrefixes();
                    List<String> excludes = excludedPrefixes == null ? List.of() : excludedPrefixes;
                    
                    int routeConfigCount = 0;
                    for (VertxRouterConfigurator routeConfigurator : sortedRoutes) {
                        String pkg = routeConfigurator.getClass().getPackageName();
                        // A configurator is "dedicated" only if its package is EXACTLY one of the annotated verticle packages
                        // OR if it's deeply nested within one.
                        // However, we should only skip it here if it's actually been picked up by a Verticle.
                        boolean isDedicated = excludes.stream().anyMatch(p -> !p.isEmpty() && pkg.startsWith(p));
                        
                        if (!isDedicated) {
                            routeConfigCount++;
                            log.trace("📋 Applying global router configurator: {} from package: {}", routeConfigurator.getClass().getName(), pkg);
                            router = IGuiceContext.get(routeConfigurator.getClass()).builder(router);
                        } else {
                            log.trace("📋 Skipping dedicated router configurator: {} (will be handled by its verticle)", routeConfigurator.getClass().getName());
                        }
                    }
                    log.debug("✅ Applied {} global router configurators", routeConfigCount);

                    log.trace("🔗 Mounting per-verticle routers");
                    List<Router> subRouters = VertxWebRouterRegistry.getSubRouters();
                    for (Router subRouter : subRouters) {
                        router.route().subRouter(subRouter);
                    }
                    log.debug("✅ Mounted {} per-verticle routers", subRouters.size());

                    log.trace("📋 Configuring Jackson ObjectMapper for JSON representation");
                    IJsonRepresentation.configureObjectMapper(DatabindCodec.mapper());

                    log.trace("🔗 Attaching router to all HTTP servers");
                    for (var server : httpServers) {
                        server.requestHandler(router);
                    }

                    log.trace("🚀 Starting HTTP/HTTPS servers");
                    int serverIndex = 0;
                    for (var server : httpServers) {
                        final int currentServerIndex = ++serverIndex;
                        log.trace("🔄 Starting server {}/{}", currentServerIndex, httpServers.size());
                        server.listen().onComplete(handler -> {
                            if (handler.failed()) {
                                log.error("❌ Failed to start server {}/{}: {}", currentServerIndex, httpServers.size(), handler.cause().getMessage(), handler.cause());
                            } else {
                                log.info("✅ Server {}/{} started successfully on port {}", currentServerIndex, httpServers.size(), handler.result().actualPort());
                            }
                        });
                    }

                    log.trace("🎉 Web server initialization completed");

                    return true;
                })
        );
    }


    /**
     * Ensures the Vert.x web server startup runs early in the post-startup chain.
     *
     * @return the ordering value for this post-startup task.
     */
    @Override
    public Integer sortOrder() {
        return Integer.MIN_VALUE + 500;
    }
}
