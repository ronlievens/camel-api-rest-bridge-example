package com.github.ronlievens.examples.camel.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

@Slf4j
public class PredicateB implements Predicate {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Override
    public boolean matches(final Exchange exchange) {
        try {
            val body = exchange.getIn().getBody(String.class);
            if (body == null || body.isBlank()) {
                return false;
            }

            val root = JSON_MAPPER.readTree(body);
            val type = root.path("type").asText(null);
            return "B".equals(type);
        } catch (JsonProcessingException e) {
            log.warn("Unable to parse JSON message", e);
        }
        return false;
    }
}
