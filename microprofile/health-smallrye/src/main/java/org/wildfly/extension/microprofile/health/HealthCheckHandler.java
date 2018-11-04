package org.wildfly.extension.microprofile.health;

import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class HealthCheckHandler implements HttpHandler {
    private final SmallRyeHealthReporter healthReporter;

    public HealthCheckHandler(SmallRyeHealthReporter healthReporter) {
        this.healthReporter = healthReporter;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        final SmallRyeHealth health = healthReporter.getHealth();
        exchange.setStatusCode(health.isDown() ? 503 : 200).getResponseHeaders().add(Headers.CONTENT_TYPE,
                "application/json");
        exchange.getResponseSender().send(health.getPayload().toString());
    }
}
