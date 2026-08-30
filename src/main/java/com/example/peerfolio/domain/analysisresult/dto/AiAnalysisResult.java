package com.example.peerfolio.domain.analysisresult.dto;

public record AiAnalysisResult(
        String riskResult,
        Integer totalRiskScore,
        String analysisComment
) {
}
