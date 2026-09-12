package com.guicedee.vertx.web;

import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import com.guicedee.client.services.lifecycle.IGuicePreDestroy;
import com.guicedee.vertx.web.spi.*;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

/** Starts additional listeners only after health/metrics initialization and awaits binding. */
public final class ManagedHttpListenersLifecycle implements IGuicePostStartup<ManagedHttpListenersLifecycle>,
        IGuicePreDestroy<ManagedHttpListenersLifecycle> {
    @Override public List<Uni<Boolean>> postLoad() {
        return List.of(Uni.createFrom().deferred(() -> {
            var listeners = ServiceLoader.load(ManagedHttpListenerProvider.class).stream()
                    .flatMap(provider -> IGuiceContext.get(provider.type()).listeners().stream()).toList();
            return Uni.createFrom().completionStage(IGuiceContext.get(ManagedHttpListenerGroup.class)
                    .start(listeners).toCompletionStage()).replaceWith(true);
        }));
    }
    @Override public void onDestroy() {
        try {
            IGuiceContext.get(ManagedHttpListenerGroup.class).close().toCompletionStage()
                    .toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Listener shutdown interrupted");
        } catch (Exception failure) {
            throw new IllegalStateException("Listener shutdown failed", failure);
        }
    }
    @Override public Integer sortOrder() { return Integer.MIN_VALUE + 800; }
}
