package com.github.ronlievens.examples.camel.route;

import com.github.ronlievens.examples.camel.predicate.PredicateA;
import com.github.ronlievens.examples.camel.predicate.PredicateB;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestApiBridgeRoute extends RouteBuilder {

    public static final String ROUTE_ID_MAIN = "route-main";
    public static final String ROUTE_ID_A = "route-A";
    public static final String ROUTE_ID_B = "route-B";

    @Override
    public void configure() {
        from("servlet:*")
            .routeId(ROUTE_ID_MAIN)
            .log("Ontvangen bericht [" + ROUTE_ID_MAIN + "]: ${body}")
            .to("direct:" + ROUTE_ID_A, "direct:" + ROUTE_ID_B)
            .end();

        from("direct:" + ROUTE_ID_A)
            .routeId(ROUTE_ID_A)
            .log("Ontvangen bericht [" + ROUTE_ID_A + "]: bridgePath=${header.CamelHttpPath} bridgeQuery=${header.CamelHttpQuery}")
            .filter(new PredicateA())
            .toD("{{api2api.endpoint.a}}${header.CamelHttpPath}?bridgeEndpoint=true");

        from("direct:" + ROUTE_ID_B)
            .routeId(ROUTE_ID_B)
            .log("Ontvangen bericht [" + ROUTE_ID_B + "]: bridgePath=${header.CamelHttpPath} bridgeQuery=${header.CamelHttpQuery}")
            .filter(new PredicateB())
            .toD("{{api2api.endpoint.b}}${header.CamelHttpPath}?bridgeEndpoint=true");
    }
}
