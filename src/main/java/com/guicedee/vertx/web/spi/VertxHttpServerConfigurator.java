package com.guicedee.vertx.web.spi;

import io.vertx.core.http.HttpServer;

/**
 * SPI hook for applying configuration to created {@link HttpServer} instances.
 */
@FunctionalInterface
public interface VertxHttpServerConfigurator
{
    /**
     * Builds and returns an updated {@link HttpServer} instance.
     *
     * @param builder the current server instance to customize.
     * @return the updated server instance.
     */
    HttpServer builder(HttpServer builder);
}
