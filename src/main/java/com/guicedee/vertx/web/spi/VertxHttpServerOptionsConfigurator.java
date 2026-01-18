package com.guicedee.vertx.web.spi;

import io.vertx.core.http.HttpServerOptions;

/**
 * SPI hook for customizing {@link HttpServerOptions} before servers are created.
 */
@FunctionalInterface
public interface VertxHttpServerOptionsConfigurator
{
    /**
     * Builds and returns an updated {@link HttpServerOptions} instance.
     *
     * @param builder the current options instance to customize.
     * @return the updated options instance.
     */
    HttpServerOptions builder(HttpServerOptions builder);
}
