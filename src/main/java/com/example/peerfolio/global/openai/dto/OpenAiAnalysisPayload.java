package com.example.peerfolio.global.openai.dto;

public record OpenAiAnalysisPayload(
        RiskResult riskResult,
        Integer totalRiskScore,
        String analysisComment
) {

    public record RiskResult(
            String riskLevel,
            Integer incomeBalanceRiskScore,
            Integer debtRiskScore,
            Integer investmentConcentrationRiskScore,
            String summary
    ) {

    }
}
