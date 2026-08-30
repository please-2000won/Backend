package com.example.peerfolio.global.openai.dto;


import java.util.Map;

public record OpenAiRequest(
        String model,
        String instructions,
        String input,
        TextConfig text
) {

    public record TextConfig(
            Format format
    ) {

    }

    public record Format(
            String type,
            String name,
            boolean strict,
            Map<String, Object> schema
    ) {
        public static Format jsonSchema(
                String name,
                Map<String, Object> schema
        ) {
            return new Format(
                    "json_schema",
                    name,
                    true,
                    schema
            );
        }
    }

    public static OpenAiRequest create(
            String model,
            String instructions,
            String input,
            Map<String, Object> schema
    ) {
        return new OpenAiRequest(
                model,
                instructions,
                input,
                new TextConfig(
                        Format.jsonSchema(
                                "peer_financial_analysis",
                                schema
                        )
                )
        );
    }
}
