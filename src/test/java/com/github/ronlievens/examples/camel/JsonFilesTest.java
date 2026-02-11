package com.github.ronlievens.examples.camel;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.ronlievens.examples.camel.util.AssertUtils.readFileAsStringFromClasspath;
import static org.assertj.core.api.Assertions.assertThat;

class JsonFilesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testMessageA() throws Exception {
        testMessage("a");
    }

    @Test
    void testMessageB() throws Exception {
        testMessage("b");
    }

    @Test
    void testMessageX() throws Exception {
        testMessage("x");
    }

    void testMessage(@NonNull String type) throws Exception {
        val jsonMessage = readFileAsStringFromClasspath("route-message-%s.json".formatted(type));
        JsonNode root = objectMapper.readTree(jsonMessage);

        assertThat(root.has("type")).as("Expected root JSON object to contain field 'type'").isTrue();
        assertThat(root.get("type").isTextual()).as("Expected root.type to be a JSON string").isTrue();
        assertThat(root.get("type").asText()).isEqualTo(type.toUpperCase());
    }
}

