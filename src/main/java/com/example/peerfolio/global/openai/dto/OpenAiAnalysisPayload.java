package com.example.peerfolio.global.openai.dto;

public record OpenAiAnalysisPayload(
        RiskResult riskResult,
        String analysisComment
) {

    public record RiskResult(
            String summary
    ) {

    }
}
