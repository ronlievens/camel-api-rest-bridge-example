package com.github.ronlievens.examples.camel.predicate;


import lombok.NonNull;
import lombok.val;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;

import java.io.IOException;

import static com.github.ronlievens.examples.camel.util.AssertUtils.readFileAsStringFromClasspath;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class PredicateAbstractTest {

    protected abstract Predicate getPredicateToTest();

    protected Exchange loadExchange(@NonNull String type) throws IOException {
        val body = readFileAsStringFromClasspath("route-message-%s.json".formatted(type));
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(body);
        return exchange;
    }

    protected void evaluatePredicate(@NonNull String type, boolean isMatch) throws Exception {
        val result = getPredicateToTest().matches(loadExchange(type));
        assertThat(result).isEqualTo(isMatch);
    }
}
