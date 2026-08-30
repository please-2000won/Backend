package com.example.peerfolio.global.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.result.Output;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiResponse(
        String id,
        String status,
        List<Output> output
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output(
            String type,
            List<Content> content
    ) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(
            String type,
            String text,
            String refusal
    ) {

    }

    public String extractOutputText() {
        if (output == null) {
            return null;
        }

        return output.stream()
                .filter(item -> item.content() != null)
                .flatMap(item -> item.content().stream())
                .filter(content -> "output_text".equals(content.type()))
                .map(Content::text)
                .findFirst()
                .orElse(null);
    }

    public boolean hasRefusal() {
        if (output == null) {
            return false;
        }

        return output.stream()
                .filter(item -> item.content() != null)
                .flatMap(item -> item.content().stream())
                .anyMatch(content ->
                                "refusal".equals(content.type())
                                        || content.refusal() != null
                );
    }
}
