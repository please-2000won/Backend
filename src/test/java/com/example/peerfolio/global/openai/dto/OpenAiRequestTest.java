package com.example.peerfolio.global.openai.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class OpenAiRequestTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void requestContainsStoreFalse() throws Exception {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(),
                "required", List.of()
        );

        OpenAiRequest request = OpenAiRequest.create(
                "test-model",
                "test-instructions",
                "{}",
                schema
        );

        String requestJson =
                objectMapper.writeValueAsString(request);

        JsonNode jsonNode =
                objectMapper.readTree(requestJson);

        assertTrue(jsonNode.has("store"));
        assertFalse(jsonNode.get("store").asBoolean());
    }
}
