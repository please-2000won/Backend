package com.example.peerfolio.domain.analysisresult.risk;

import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import org.springframework.stereotype.Component;

@Component
public class RiskScoreCalculator {

    private static final int MIN_RISK_SCORE = 0;
    private static final int MAX_RISK_SCORE = 100;

    public RiskScoreResult calculateRiskScore(
            PeerProfileData profile,
            PeerAssetData asset
    ) {
        double totalInvestmentAmount =
                (double) asset.depositBondAmount()
                        + asset.domesticStockAmount()
                        + asset.foreignStockAmount()
                        + asset.alternativeAmount();

        double totalStockAmount =
                (double) asset.domesticStockAmount()
                        + asset.foreignStockAmount();

        double stockRatio = calculateRatio(
                totalStockAmount,
                totalInvestmentAmount
        );

        double alternativeRatio = calculateRatio(
                asset.alternativeAmount(),
                totalInvestmentAmount
        );

        double stockScore =
                calculateStockScore(stockRatio);

        double alternativeScore =
                calculateAlternativeScore(alternativeRatio);

        // 주식과 대체·고위험자산 점수 중 큰 값을 투자자산 점수로 사용
        double investmentScore =
                Math.max(stockScore, alternativeScore);

        double debtScore = calculateDebtScore(
                profile.totalAssetAmount(),
                profile.totalDebtAmount()
        );

        double fixedExpenseScore =
                calculateFixedExpenseScore(
                        profile.monthlyIncome(),
                        profile.fixedExpense()
                );

        int totalRiskScore = calculateTotalRiskScore(
                investmentScore,
                debtScore,
                fixedExpenseScore
        );

        RiskLevel riskLevel =
                RiskLevel.from(totalRiskScore);

        RiskScoreDetail detail = new RiskScoreDetail(
                stockRatio,
                stockScore,
                alternativeRatio,
                alternativeScore,
                investmentScore,
                calculateNullableRatio(
                        profile.totalDebtAmount(),
                        profile.totalAssetAmount()
                ),
                debtScore,
                calculateNullableRatio(
                        profile.fixedExpense(),
                        profile.monthlyIncome()
                ),
                fixedExpenseScore
        );

        return new RiskScoreResult(
                totalRiskScore,
                riskLevel,
                detail
        );
    }

    private double calculateStockScore(double ratio) {
        if (ratio <= 0) {
            return 0;
        }

        if (ratio < 80) {
            return ratio / 80.0 * 60.0;
        }

        return 60;
    }

    private double calculateAlternativeScore(double ratio) {
        if (ratio <= 0) {
            return 0;
        }

        if (ratio < 50) {
            return ratio / 50.0 * 60.0;
        }

        return 60;
    }

    private double calculateDebtScore(
            long cashAmount,
            long debtAmount
    ) {
        if (debtAmount <= 0) {
            return 0;
        }

        if (cashAmount <= 0) {
            return 20;
        }

        double debtRatio =
                (double) debtAmount / cashAmount * 100.0;

        if (debtRatio <= 30) {
            return debtRatio / 30.0 * 5.0;
        }

        if (debtRatio <= 100) {
            return 5.0
                    + (debtRatio - 30.0) / 70.0 * 10.0;
        }

        if (debtRatio <= 150) {
            return 15.0
                    + (debtRatio - 100.0) / 50.0 * 5.0;
        }

        return 20;
    }

    private double calculateFixedExpenseScore(
            long monthlyIncome,
            long fixedExpense
    ) {
        if (monthlyIncome <= 0) {
            return 20;
        }

        double expenseRatio =
                (double) fixedExpense / monthlyIncome * 100.0;

        if (expenseRatio <= 40) {
            return 0;
        }

        if (expenseRatio <= 80) {
            return (expenseRatio - 40.0) / 20.0 * 5.0;
        }

        if (expenseRatio <= 100) {
            return 10.0
                    + (expenseRatio - 80.0) / 20.0 * 10.0;
        }

        return 20;
    }

    private int calculateTotalRiskScore(
            double investmentScore,
            double debtScore,
            double fixedExpenseScore
    ) {
        int roundedScore = (int) Math.round(
                investmentScore
                        + debtScore
                        + fixedExpenseScore
        );

        return Math.max(
                MIN_RISK_SCORE,
                Math.min(MAX_RISK_SCORE, roundedScore)
        );
    }

    private double calculateRatio(
            double amount,
            double totalAmount
    ) {
        if (totalAmount <= 0) {
            return 0;
        }

        return amount / totalAmount * 100.0;
    }

    private Double calculateNullableRatio(
            long numerator,
            long denominator
    ) {
        if (denominator <= 0) {
            return null;
        }

        return (double) numerator / denominator * 100.0;
    }
}
