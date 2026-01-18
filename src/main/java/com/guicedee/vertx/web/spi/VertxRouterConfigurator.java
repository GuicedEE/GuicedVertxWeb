package com.guicedee.vertx.web.spi;

import com.guicedee.client.services.IDefaultService;
import io.vertx.ext.web.Router;

/**
 * SPI hook for registering routes and handlers on a {@link Router}.
 */
public interface VertxRouterConfigurator<J extends VertxRouterConfigurator<J>> extends IDefaultService<J>
{
    /**
     * Builds and returns an updated {@link Router} instance.
     *
     * @param builder the current router instance to customize.
     * @return the updated router instance.
     */
    Router builder(Router builder);
}
