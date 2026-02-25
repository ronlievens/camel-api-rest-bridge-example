package com.github.ronlievens.examples.camel.route;

import com.github.ronlievens.examples.camel.predicate.PredicateA;
import com.github.ronlievens.examples.camel.predicate.PredicateB;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestApiBridgeRoute extends RouteBuilder {

    public static final String ROUTE_ID_A = "route-A";
    public static final String ROUTE_ID_B = "route-B";

    @Override
    public void configure() {
        from("servlet:*")
            .to("direct:processRoute");

        from("direct:processRoute")
            .routeId("main-route")
            .log("Ontvangen bericht: ${body}")
            .to("direct:routeA", "direct:routeB")
            .end();

        from("direct:routeA")
            .routeId(ROUTE_ID_A)
            .log("Route A ontvangen: bridgePath=${header.CamelHttpPath} bridgeQuery=${header.CamelHttpQuery}")
            .filter(new PredicateA())
            .toD("{{api2api.endpoint.a}}${header.CamelHttpPath}?bridgeEndpoint=true");

        from("direct:routeB")
            .routeId(ROUTE_ID_B)
            .log("Route B ontvangen")
            .filter(new PredicateB())
            .toD("{{api2api.endpoint.b}}?bridgeEndpoint=true");
    }
}
