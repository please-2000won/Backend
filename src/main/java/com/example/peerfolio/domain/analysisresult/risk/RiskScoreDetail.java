package com.example.peerfolio.domain.analysisresult.risk;

public record RiskScoreDetail(
        double stockRatio,
        double stockScore,

        double alternativeRatio,
        double alternativeScore,

        double investmentScore,

        Double debtToCashRatio,
        double debtScore,

        Double fixedExpenseRatio,
        double fixedExpenseScore
) {
}
