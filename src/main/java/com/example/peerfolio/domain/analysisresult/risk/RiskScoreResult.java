package com.example.peerfolio.domain.analysisresult.risk;

public record RiskScoreResult(
        int totalRiskScore,
        RiskLevel riskLevel,
        RiskScoreDetail riskScoreDetail
) {
}
