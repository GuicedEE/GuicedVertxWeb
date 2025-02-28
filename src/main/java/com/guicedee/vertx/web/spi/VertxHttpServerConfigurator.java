package com.guicedee.vertx.web.spi;

import io.vertx.core.http.HttpServer;

@FunctionalInterface
public interface VertxHttpServerConfigurator
{
    HttpServer builder(HttpServer builder);
}
