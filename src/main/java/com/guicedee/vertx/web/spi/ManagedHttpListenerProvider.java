package com.guicedee.vertx.web.spi;

import java.util.List;

/** Supplies extra isolated listeners to the GuicedEE startup/shutdown lifecycle. */
public interface ManagedHttpListenerProvider {
    List<ManagedHttpListener> listeners();
}
