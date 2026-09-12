import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import com.guicedee.vertx.web.VertxWebServerPostStartup;

module com.guicedee.vertx.web {
    uses com.guicedee.vertx.web.spi.VertxRouterConfigurator;
    uses com.guicedee.vertx.web.spi.VertxHttpServerConfigurator;
    uses com.guicedee.vertx.web.spi.VertxHttpServerOptionsConfigurator;
    uses com.guicedee.vertx.web.spi.ManagedHttpListenerProvider;

    exports com.guicedee.vertx.web.spi;

    requires transitive com.guicedee.vertx;
    requires transitive io.vertx.web;
    requires transitive io.vertx.core;
    requires static lombok;

    provides IGuicePostStartup with VertxWebServerPostStartup, com.guicedee.vertx.web.ManagedHttpListenersLifecycle;
    provides com.guicedee.client.services.lifecycle.IGuicePreDestroy with com.guicedee.vertx.web.ManagedHttpListenersLifecycle;

    opens com.guicedee.vertx.web to com.google.guice;
}
