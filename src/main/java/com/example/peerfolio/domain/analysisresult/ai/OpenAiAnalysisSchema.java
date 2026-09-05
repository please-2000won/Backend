package com.example.peerfolio.domain.analysisresult.ai;

import java.util.List;
import java.util.Map;

final class OpenAiAnalysisSchema {

    private OpenAiAnalysisSchema() {
    }

    static Map<String, Object> create() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "riskResult", riskResultSchema(),
                        "analysisComment", analysisCommentSchema()
                ),
                "required", List.of(
                        "riskResult",
                        "analysisComment"
                )
        );
    }

    private static Map<String, Object> riskResultSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "summary", Map.of(
                                "type", "string",
                                "minLength", 1
                        )
                ),
                "required", List.of(
                        "summary"
                )
        );
    }

    private static Map<String, Object> analysisCommentSchema() {
        return Map.of(
                "type", "string",
                "minLength", 1
        );
    }
}
