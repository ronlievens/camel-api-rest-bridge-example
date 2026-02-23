package com.github.ronlievens.examples.camel.route;

import com.github.ronlievens.examples.camel.predicate.PredicateA;
import com.github.ronlievens.examples.camel.predicate.PredicateB;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestApiBridgeRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("platform-http:/*")
            .to("direct:processRoute");

        from("direct:processRoute")
            .routeId("main-route")
            .log("Ontvangen bericht: ${body}")
            .to("direct:routeA", "direct:routeB")
            .end();

        from("direct:routeA")
            .routeId("route-A")
            .log("Route A ontvangen: bridgePath=${exchangeProperty.bridgePath} bridgeQuery=${exchangeProperty.bridgeQuery}")
            .filter(new PredicateA())
            .toD("{{api2api.endpoint.a}}${header.CamelHttpPath}?bridgeEndpoint=true");

        from("direct:routeB")
            .routeId("route-B")
            .log("Route B ontvangen")
            .filter(new PredicateB())
            .toD("{{api2api.endpoint.b}}?bridgeEndpoint=true");
    }
}
