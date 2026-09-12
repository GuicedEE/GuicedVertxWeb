package com.guicedee.vertx.web.spi;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import java.util.Objects;
import java.util.function.Function;

/** An isolated listener; global router/server configurators are deliberately not applied. */
public record ManagedHttpListener(String name, HttpServerOptions options, Function<Vertx, Router> router) {
    public ManagedHttpListener {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Listener name is required");
        options = new HttpServerOptions(Objects.requireNonNull(options));
        Objects.requireNonNull(router);
    }
    @Override public HttpServerOptions options() { return new HttpServerOptions(options); }
}
