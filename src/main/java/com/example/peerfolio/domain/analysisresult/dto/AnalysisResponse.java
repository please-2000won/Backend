package com.example.peerfolio.domain.analysisresult.dto;

import java.time.LocalDateTime;

public record AnalysisResponse(
        Long analysisResultId,
        Integer peerCount,
        BenchmarkResult benchmarkResult,
        RiskResult riskResult,
        Integer totalRiskScore,
        String analysisComment,
        LocalDateTime createdAt
) {

    public record RiskResult(
            String riskLevel,
            String summary
    ) {
    }
}
