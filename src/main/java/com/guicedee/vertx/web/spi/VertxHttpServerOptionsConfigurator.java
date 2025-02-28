package com.guicedee.vertx.web.spi;

import io.vertx.core.http.HttpServerOptions;

@FunctionalInterface
public interface VertxHttpServerOptionsConfigurator
{
    HttpServerOptions builder(HttpServerOptions builder);
}
