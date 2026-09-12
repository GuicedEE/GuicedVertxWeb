module com.guicedee.vertx.web.test {
    requires com.guicedee.vertx.web;
    requires org.junit.jupiter.api;
    requires java.net.http;
    opens com.guicedee.vertx.web.test to org.junit.platform.commons;
}
