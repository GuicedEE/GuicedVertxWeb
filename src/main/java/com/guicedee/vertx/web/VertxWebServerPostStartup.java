package com.guicedee.vertx.web;

import com.google.inject.Inject;
import com.guicedee.client.Environment;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import com.guicedee.client.scopes.CallScoper;
import com.guicedee.client.scopes.CallScopeProperties;
import com.guicedee.client.scopes.CallScopeSource;
import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.guicedee.vertx.web.spi.VertxHttpServerConfigurator;
import com.guicedee.vertx.web.spi.VertxHttpServerOptionsConfigurator;
import com.guicedee.vertx.web.spi.VertxRouterConfigurator;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
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
 *   <li>HTTPS_KEYSTORE_PASSWORD</li>
 * </ul>
 */
@Log4j2
public class VertxWebServerPostStartup implements IGuicePostStartup<VertxWebServerPostStartup> {
    @Inject
    private io.vertx.core.Vertx vertx;

    /**
     * Initializes server options, creates and configures HTTP/HTTPS servers, builds the router,
     * and starts listening asynchronously.
     *
     * @return a list containing a single {@link Future} that completes once startup is scheduled.
     */
    @Override
    public List<Future<Boolean>> postLoad() {
        log.info("🚀 Starting Vertx Web Server initialization");
        log.debug("📋 Creating server options with default configuration");
        return List.of(vertx.executeBlocking(() -> {
            CallScoper callScoper = null;
            boolean started = false;
            try {
                callScoper = IGuiceContext.get(CallScoper.class);
                if (!callScoper.isStartedScope()) {
                    callScoper.enter();
                    started = true;
                }
                CallScopeProperties props = IGuiceContext.get(CallScopeProperties.class);
                if (props.getSource() == null || props.getSource() == CallScopeSource.Unknown) {
                    props.setSource(CallScopeSource.Startup);
                }
                HttpServerOptions serverOptions = new HttpServerOptions();
                serverOptions.setCompressionSupported(true);
                serverOptions.setCompressionLevel(9);
                serverOptions.setTcpKeepAlive(true);
                serverOptions.setMaxHeaderSize(65536);
                serverOptions.setMaxChunkSize(65536);
                serverOptions.setMaxFormAttributeSize(65536);
                serverOptions.setMaxFormFields(-1);
                serverOptions.setMaxInitialLineLength(65536);
                log.debug("📋 Default server options configured - Compression: enabled(level 9), TCP KeepAlive: true, MaxHeaderSize: 65536 bytes, MaxInitialLineLength: 65536 bytes");

                log.debug("🔍 Loading VertxHttpServerOptionsConfigurator services");
                ServiceLoader<VertxHttpServerOptionsConfigurator> options = ServiceLoader.load(VertxHttpServerOptionsConfigurator.class);
                for (VertxHttpServerOptionsConfigurator option : options) {
                    log.debug("📋 Applying server options from configurator: {}", option.getClass().getName());
                    serverOptions = option.builder(IGuiceContext.get(serverOptions.getClass()));
                }

                log.debug("📋 Preparing HTTP/HTTPS server creation");
                List<HttpServer> httpServers = new ArrayList<>();

                // HTTP Server setup
                boolean httpEnabled = Boolean.parseBoolean(Environment.getProperty("HTTP_ENABLED", "true"));
                if (httpEnabled) {
                    int httpPort = Integer.parseInt(Environment.getSystemPropertyOrEnvironment("HTTP_PORT", "8080"));
                    log.info("🚀 Creating HTTP server on port: {}", httpPort);
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
                    log.info("🚀 Creating HTTPS server on port: {}", httpsPort);

                    // Configure SSL
                    serverOptions.setSsl(true).setUseAlpn(true);
                    serverOptions.setPort(httpsPort);

                    String keystorePath = Environment.getSystemPropertyOrEnvironment("HTTPS_KEYSTORE", "");
                    log.debug("📋 Using keystore: {}", keystorePath);

                    if (keystorePath.toLowerCase().endsWith("pfx") ||
                            keystorePath.toLowerCase().endsWith("p12") ||
                            keystorePath.toLowerCase().endsWith("p8")) {
                        log.debug("🔐 Configuring PFX/PKCS12 keystore");
                        String keystorePassword = Environment.getSystemPropertyOrEnvironment("HTTPS_KEYSTORE_PASSWORD", "");
                        serverOptions.setKeyCertOptions(new PfxOptions()
                                .setPassword(keystorePassword)
                                .setPath(keystorePath));
                    } else if (keystorePath.toLowerCase().endsWith("jks")) {
                        log.debug("🔐 Configuring JKS keystore");
                        String keystorePassword = Environment.getSystemPropertyOrEnvironment("HTTPS_KEYSTORE_PASSWORD", "changeit");
                        serverOptions.setKeyCertOptions(new JksOptions()
                                .setPassword(keystorePassword)
                                .setPath(keystorePath));
                    } else {
                        log.warn("⚠️ No valid keystore format detected for path: {}", keystorePath);
                    }

                    var server = vertx.createHttpServer(serverOptions);
                    httpServers.add(server);
                    log.debug("✅ HTTPS server created successfully");
                } else {
                    log.warn("📋 HTTPS server disabled by configuration");
                }

                log.debug("📊 Server summary: HTTP enabled: {}, HTTPS enabled: {}, Total servers: {}",
                        httpEnabled, httpsEnabled, httpServers.size());

                log.debug("🔍 Loading VertxHttpServerConfigurator services");
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

                log.info("🔗 Creating and configuring Vertx Router");
                Router router = Router.router(vertx);

                long maxBodySize = Long.parseLong(Environment.getSystemPropertyOrEnvironment("VERTX_MAX_BODY_SIZE", String.valueOf(500L * 1024 * 1024)));
                log.debug("📋 Setting up BodyHandler with uploads directory: 'uploads', deleteUploadedFilesOnEnd: true, bodyLimit: {} bytes, handleFileUploads: true", maxBodySize);
                router.route().handler(BodyHandler.create()
                        .setUploadsDirectory("uploads")
                        .setDeleteUploadedFilesOnEnd(true)
                        .setHandleFileUploads(true)
                        .setBodyLimit(maxBodySize)
                        .setMergeFormAttributes(true));

                log.debug("🔍 Loading VertxRouterConfigurator services");
                ServiceLoader<VertxRouterConfigurator> routes = ServiceLoader.load(VertxRouterConfigurator.class);
                List<VertxRouterConfigurator> sortedRoutes = new ArrayList<>();
                routes.forEach(sortedRoutes::add);
                sortedRoutes.sort(VertxRouterConfigurator::compareTo);
                int routeConfigCount = 0;
                for (VertxRouterConfigurator routeConfigurator : sortedRoutes) {
                    routeConfigCount++;
                    log.debug("📋 Applying router configurator: {} with sort order {}", routeConfigurator.getClass().getName(), routeConfigurator.sortOrder());
                    router = IGuiceContext.get(routeConfigurator.getClass()).builder(router);
                }
                log.debug("✅ Applied {} router configurators", routeConfigCount);

                log.debug("📋 Configuring Jackson ObjectMapper for JSON representation");
                IJsonRepresentation.configureObjectMapper(DatabindCodec.mapper());

                log.debug("🔗 Attaching router to all HTTP servers");
                for (var server : httpServers) {
                    server.requestHandler(router);
                }

                log.info("🚀 Starting HTTP/HTTPS servers");
                int serverIndex = 0;
                for (var server : httpServers) {
                    final int currentServerIndex = ++serverIndex;
                    log.debug("🔄 Starting server {}/{}", currentServerIndex, httpServers.size());
                    server.listen().onComplete(handler -> {
                        if (handler.failed()) {
                            log.error("❌ Failed to start server {}/{}: {}", currentServerIndex, httpServers.size(), handler.cause().getMessage(), handler.cause());
                        } else {
                            log.info("✅ Server {}/{} started successfully on port {}", currentServerIndex, httpServers.size(), handler.result().actualPort());
                        }
                    });
                }

                log.info("🎉 Web server initialization completed");

                return true;
            } finally {
                if (started && callScoper != null) {
                    callScoper.exit();
                }
            }
        }, false));
    }

    /**
     * Ensures the Vert.x web server startup runs early in the post-startup chain.
     *
     * @return the ordering value for this post-startup task.
     */
    @Override
    public Integer sortOrder() {
        int order = Integer.MIN_VALUE + 500;
        log.debug("📋 VertxWebServerPostStartup sortOrder: {}", order);
        return order;
    }
}
