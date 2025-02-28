import com.guicedee.vertx.web.VertxWebServerPostStartup;

module com.guicedee.vertx.web {
    uses com.guicedee.vertx.web.spi.VertxRouterConfigurator;
    uses com.guicedee.vertx.web.spi.VertxHttpServerConfigurator;
    uses com.guicedee.vertx.web.spi.VertxHttpServerOptionsConfigurator;

    exports com.guicedee.vertx.web.spi;

    requires transitive com.guicedee.vertx;
    requires transitive io.vertx.web;
    requires transitive io.vertx.core;
    requires static lombok;

    provides com.guicedee.guicedinjection.interfaces.IGuicePostStartup with VertxWebServerPostStartup;

    opens com.guicedee.vertx.web to com.google.guice;
}