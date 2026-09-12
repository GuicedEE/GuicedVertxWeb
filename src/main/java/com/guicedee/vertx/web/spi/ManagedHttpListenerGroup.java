package com.guicedee.vertx.web.spi;

import com.google.inject.Singleton;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import java.util.*;

/** Owns listener startup, rollback and shutdown. A group is single-use. */
@Singleton
public final class ManagedHttpListenerGroup {
    private final List<HttpServer> servers = new ArrayList<>();
    private Vertx runtime;
    private Future<Map<String, Integer>> started;
    private Future<Void> closed;

    public synchronized Future<Map<String, Integer>> start(List<ManagedHttpListener> listeners) {
        if (closed != null) return Future.failedFuture("Listener group is closed");
        if (started != null) return started;
        var definitions = List.copyOf(listeners);
        var names = new HashSet<String>();
        var configuredPorts = new HashSet<Integer>();
        for (var listener : definitions) {
            if (!names.add(listener.name())) return Future.failedFuture("Duplicate listener name");
            int port = listener.options().getPort();
            if (port < 0 || (port > 0 && !configuredPorts.add(port)))
                return Future.failedFuture("Managed listeners require distinct non-negative ports");
        }
        // Vert.x shares same-address servers within a runtime. A dedicated runtime
        // keeps these listeners outside the application's public socket registry.
        if (!definitions.isEmpty()) runtime = Vertx.vertx(new VertxOptions()
                .setEventLoopPoolSize(1).setWorkerPoolSize(1).setInternalBlockingPoolSize(1));
        Future<Map<String, Integer>> chain = Future.succeededFuture(new LinkedHashMap<>());
        for (var listener : definitions) {
            chain = chain.compose(ports -> {
                var router = Objects.requireNonNull(listener.router().apply(runtime));
                var server = runtime.createHttpServer(listener.options().setReusePort(false).setReuseAddress(false))
                        .webSocketHandshakeHandler(handshake -> handshake.reject(403))
                        .requestHandler(router);
                servers.add(server);
                return server.listen().map(bound -> { ports.put(listener.name(), bound.actualPort()); return ports; });
            });
        }
        started = chain.map(Map::copyOf).recover(failure -> closeServers()
                .transform(ignored -> Future.failedFuture(failure)));
        return started;
    }

    public synchronized Future<Void> close() {
        if (closed != null) return closed;
        // Wait for an in-progress bind (or its rollback) before closing, so a late
        // successful listen cannot reopen the group after shutdown.
        closed = started == null ? Future.succeededFuture()
                : started.transform(ignored -> closeServers());
        return closed;
    }

    private Future<Void> closeServers() {
        List<Future<Void>> closes = servers.stream().map(HttpServer::close).toList();
        servers.clear();
        Future<Void> sockets = Future.join(closes).mapEmpty();
        var owned = runtime;
        runtime = null;
        return owned == null ? sockets : sockets.transform(result -> owned.close().transform(shutdown ->
                result.failed() ? Future.<Void>failedFuture(result.cause())
                        : shutdown.failed() ? Future.<Void>failedFuture(shutdown.cause()) : Future.<Void>succeededFuture()));
    }
}
