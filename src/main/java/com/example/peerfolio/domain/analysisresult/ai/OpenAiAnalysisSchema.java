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
                        "totalRiskScore", scoreSchema(),
                        "analysisComment", Map.of(
                                "type", "string",
                                "minLength", 1
                        )
                ),
                "required", List.of(
                        "riskResult",
                        "totalRiskScore",
                        "analysisComment"
                )
        );
    }

    private static Map<String, Object> riskResultSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "riskLevel", Map.of(
                                "type", "string",
                                "enum", List.of(
                                        "LOW",
                                        "MEDIUM",
                                        "HIGH"
                                )
                        ),
                        "summary", Map.of(
                                "type", "string",
                                "minLength", 1
                        )
                ),
                "required", List.of(
                        "riskLevel",
                        "summary"
                )
        );
    }

    private static Map<String, Object> scoreSchema() {
        return Map.of(
                "type", "integer",
                "minimum", 0,
                "maximum", 100
        );
    }
}
