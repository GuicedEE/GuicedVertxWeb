package com.guicedee.vertx.web;

import com.guicedee.client.IGuiceContext;
import com.guicedee.vertx.spi.VerticleBuilder;
import com.guicedee.vertx.spi.VerticleStartup;
import com.guicedee.vertx.web.spi.VertxRouterConfigurator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

/**
 * Verticle startup hook that registers a router for the verticle's assigned package.
 */
@Log4j2
public class VertxWebVerticleStartup implements VerticleStartup<VertxWebVerticleStartup> {

    @Override
    public void start(Promise<Void> startPromise, Vertx vertx, AbstractVerticle verticle, String assignedPackage) {
        try {
            log.debug("Starting VertxWebVerticleStartup for package: {}", assignedPackage);
            
            // Build inclusion predicate matching current assigned package
            var excludedPrefixes = VerticleBuilder.getAnnotatedPrefixes();
            Predicate<String> includeByPackage;
            if (assignedPackage != null && !assignedPackage.isEmpty()) {
                includeByPackage = pkg -> pkg != null && pkg.startsWith(assignedPackage);
            } else {
                // If this is the "default" verticle (assignedPackage is empty), 
                // it should only pick up configurators that are NOT in any of the annotated prefixes
                List<String> excludes = excludedPrefixes == null ? List.of() : excludedPrefixes;
                includeByPackage = pkg -> pkg != null && excludes.stream().noneMatch(p -> !p.isEmpty() && pkg.startsWith(p));
            }

            ServiceLoader<VertxRouterConfigurator> routes = ServiceLoader.load(VertxRouterConfigurator.class);
            List<VertxRouterConfigurator> matchingRoutes = new ArrayList<>();
            for (VertxRouterConfigurator routeConfigurator : routes) {
                String pkg = routeConfigurator.getClass().getPackageName();
                if (includeByPackage.test(pkg)) {
                    matchingRoutes.add(routeConfigurator);
                }
            }

            if (!matchingRoutes.isEmpty()) {
                log.info("Creating dedicated router for Verticle package: '{}' with {} matching configurators", 
                        (assignedPackage == null || assignedPackage.isEmpty() ? "default" : assignedPackage), 
                        matchingRoutes.size());
                Router subRouter = Router.router(vertx);
                
                matchingRoutes.sort(VertxRouterConfigurator::compareTo);
                for (VertxRouterConfigurator routeConfigurator : matchingRoutes) {
                    log.debug("Applying router configurator {} to router for package: '{}'", 
                            routeConfigurator.getClass().getName(), assignedPackage);
                    
                    // Try to set package filter if the configurator supports it
                    try {
                        var method = routeConfigurator.getClass().getMethod("setPackageFilter", String.class);
                        method.invoke(routeConfigurator, assignedPackage);
                    } catch (Exception ignored) {
                        // Not all configurators support package filtering
                    }

                    subRouter = IGuiceContext.get(routeConfigurator.getClass()).builder(subRouter);
                }

                // If this is a worker verticle, we should probably ensure all routes are blocking
                // However, Vert.x Router doesn't have a simple "make all routes blocking" switch.
                // But since it's running in a worker verticle, the handlers will naturally run on worker threads 
                // IF they are called from this verticle. 
                // But the request handler is called by the HTTP server, which is usually on an event loop.
                
                VertxWebRouterRegistry.addRouter(subRouter);
                log.debug("Dedicated router for {} registered", assignedPackage);
            }
        } catch (Throwable t) {
            log.error("Failed to configure dedicated router for package: {}", assignedPackage, t);
        }
    }
}
