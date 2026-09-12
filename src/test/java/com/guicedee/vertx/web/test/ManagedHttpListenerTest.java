package com.guicedee.vertx.web.test;

import com.guicedee.vertx.web.spi.*;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class ManagedHttpListenerTest {
    private static <T> T await(Future<T> value) throws Exception {
        return value.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
    private ManagedHttpListener listener(String name, int port, String path) {
        return new ManagedHttpListener(name, new HttpServerOptions().setHost("127.0.0.1").setPort(port).setReusePort(false), vertx -> {
            var router = Router.router(vertx);
            router.get(path).handler(ctx -> ctx.response().end(name));
            return router;
        });
    }
    @Test void isolatedRoutersBindAndCloseWithoutSharingPublicRoutes() throws Exception {
        var vertx = Vertx.vertx(); var group = new ManagedHttpListenerGroup();
        try (var client = HttpClient.newHttpClient()) {
            var ports = await(group.start(List.of(listener("public",0,"/page"),listener("private",0,"/metrics"))));
            for (String name : ports.keySet()) {
                String own = name.equals("public") ? "/page" : "/metrics";
                String other = name.equals("public") ? "/metrics" : "/page";
                var base = "http://127.0.0.1:" + ports.get(name);
                assertEquals(200, client.send(HttpRequest.newBuilder(URI.create(base+own)).build(),HttpResponse.BodyHandlers.discarding()).statusCode());
                assertEquals(404, client.send(HttpRequest.newBuilder(URI.create(base+other)).build(),HttpResponse.BodyHandlers.discarding()).statusCode());
            }
            await(group.close()); await(group.close());
            assertThrows(Exception.class, () -> await(group.start(List.of())));
            for (int port : ports.values()) {
                // Socket rebind proves closure, independent of client connection caches.
                var rebound = await(vertx.createHttpServer().requestHandler(request -> request.response().end()).listen(port,"127.0.0.1")); await(rebound.close());
            }
        } finally { await(group.close()); await(vertx.close()); }
    }
    @Test void laterStartupFailureRollsBackEarlierListeners() throws Exception {
        var vertx = Vertx.vertx(); var group = new ManagedHttpListenerGroup();
        try {
            var reserved = await(vertx.createHttpServer().requestHandler(request -> request.response().end()).listen(0,"127.0.0.1"));
            int port = reserved.actualPort(); await(reserved.close());
            var broken = new ManagedHttpListener("broken",new HttpServerOptions(),v -> {throw new IllegalStateException("fixture");});
            assertThrows(Exception.class, () -> await(group.start(List.of(listener("first",port,"/"),broken))));
            var rebound = await(vertx.createHttpServer().requestHandler(request -> request.response().end()).listen(port,"127.0.0.1")); await(rebound.close());
        } finally { await(group.close()); await(vertx.close()); }
    }
    @Test void occupiedPortIsAStartupFailureAndNeverAReportedSuccess() throws Exception {
        var vertx = Vertx.vertx(); var group = new ManagedHttpListenerGroup();
        try (var occupied = new java.net.ServerSocket(0,1,java.net.InetAddress.getLoopbackAddress())) {
            var definition = listener("occupied", occupied.getLocalPort(), "/");
            assertThrows(Exception.class, () -> await(group.start(List.of(definition))));
        } finally { await(group.close()); await(vertx.close()); }
    }
    @Test void existingVertxPublicSocketCannotBeSharedByManagement() throws Exception {
        var vertx = Vertx.vertx(); var group = new ManagedHttpListenerGroup();
        try (var client = HttpClient.newHttpClient()) {
            var reservation = await(vertx.createHttpServer().requestHandler(request -> request.response().end())
                    .listen(0, "127.0.0.1"));
            int port = reservation.actualPort(); await(reservation.close());
            await(vertx.createHttpServer()
                    .requestHandler(request -> request.response().setStatusCode(404).end())
                    .listen(port, "127.0.0.1"));
            assertThrows(Exception.class, () -> await(group.start(List.of(listener("private",port,"/metrics")))));
            for (int i=0;i<10;i++) assertEquals(404,client.send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:"+port+"/metrics")).build(),HttpResponse.BodyHandlers.discarding()).statusCode());
        } finally { await(group.close()); await(vertx.close()); }
    }
    @Test void sameManagedPortIsRejectedEvenWithDifferentNames() throws Exception {
        var group = new ManagedHttpListenerGroup();
        try {
            assertThrows(Exception.class, () -> await(group.start(List.of(listener("one",9100,"/"),listener("two",9100,"/")))));
            assertThrows(Exception.class, () -> await(group.start(List.of(listener("shared",-1,"/")))));
        } finally { await(group.close()); }
    }
    @Test void shutdownDuringStartupDoesNotLeaveALateListener() throws Exception {
        var vertx = Vertx.vertx(); var group = new ManagedHttpListenerGroup();
        try {
            var starting = group.start(List.of(listener("private",0,"/metrics")));
            var closing = group.close(); var ports = await(starting); await(closing);
            var rebound = await(vertx.createHttpServer().requestHandler(request -> request.response().end()).listen(ports.get("private"),"127.0.0.1")); await(rebound.close());
        } finally { await(group.close()); await(vertx.close()); }
    }
    @Test void optionsAreCopiedAndDuplicateNamesAreRejected() throws Exception {
        var options = new HttpServerOptions().setHost("127.0.0.1").setPort(9100);
        var definition = new ManagedHttpListener("private",options,Router::router);
        options.setHost("0.0.0.0"); definition.options().setHost("::");
        assertEquals("127.0.0.1",definition.options().getHost());
        var vertx = Vertx.vertx(); var group = new ManagedHttpListenerGroup();
        try { assertThrows(Exception.class, () -> await(group.start(List.of(definition,definition)))); }
        finally { await(group.close()); await(vertx.close()); }
    }
}
