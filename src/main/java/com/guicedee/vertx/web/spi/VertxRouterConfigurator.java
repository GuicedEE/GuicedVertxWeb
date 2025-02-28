package com.guicedee.vertx.web.spi;

import io.vertx.ext.web.Router;

@FunctionalInterface
public interface VertxRouterConfigurator
{
    Router builder(Router builder);
}
