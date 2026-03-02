package com.guicedee.vertx.web;

import io.vertx.ext.web.Router;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for sub-routers contributed by Verticles.
 */
public class VertxWebRouterRegistry {
    private static final List<Router> subRouters = Collections.synchronizedList(new ArrayList<>());

    public static void addRouter(Router router) {
        subRouters.add(router);
    }

    public static List<Router> getSubRouters() {
        return new ArrayList<>(subRouters);
    }
}
