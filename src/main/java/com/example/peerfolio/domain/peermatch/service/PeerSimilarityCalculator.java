package com.example.peerfolio.domain.peermatch.service;

import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import org.springframework.stereotype.Component;

@Component
public class PeerSimilarityCalculator {

    // 각 금융 프로필의 항목이 전체 유사도에서 차지하는 비중
    // 수정 가능
    private static final double AGE_WEIGHT = 0.10;
    private static final double MONTHLY_INCOME_WEIGHT = 0.20;
    private static final double FIXED_EXPENSE_WEIGHT = 0.15;
    private static final double SAVINGS_GOAL_WEIGHT = 0.10;
    private static final double TOTAL_ASSET_WEIGHT = 0.30;
    private static final double TOTAL_DEBT_WEIGHT = 0.15;

    // 나이 20살 이상 차이 날 시에는 나이 유사도 0으로
    private static final double MAX_AGE_DIFFERENCE = 20.0;

    // 분석 대상 사용자와 피어 후보의 금융 프로필 유사도 계산 (범위 0.0~100.0)
    public double calculateSimilarity(
            PeerProfileData target,
            PeerProfileData candidate
    ) {
        // 나이만 별도 계산식 사용
        double ageSimilarity = calculateAgeSimilarity(
                target.age(),
                candidate.age()
        );

        // 금액 항목은 상대적 차이를 기준으로 비교
        double monthlyIncomeSimilarity = calculateAmountSimilarity(
                target.monthlyIncome(),
                candidate.monthlyIncome()
        );

        double fixedExpenseSimilarity = calculateAmountSimilarity(
                target.fixedExpense(),
                candidate.fixedExpense()
        );

        double savingsGoalSimilarity = calculateAmountSimilarity(
                target.savingsGoal(),
                candidate.savingsGoal()
        );

        double totalAssetSimilarity = calculateAmountSimilarity(
                target.totalAssetAmount(),
                candidate.totalAssetAmount()
        );

        double totalDebtSimilarity = calculateAmountSimilarity(
                target.totalDebtAmount(),
                candidate.totalDebtAmount()
        );

        // 항목별 유사도에 가중치 곱해 최종 유사도 도출
        double similarity =
                ageSimilarity * AGE_WEIGHT
                + monthlyIncomeSimilarity * MONTHLY_INCOME_WEIGHT
                + fixedExpenseSimilarity * FIXED_EXPENSE_WEIGHT
                + savingsGoalSimilarity * SAVINGS_GOAL_WEIGHT
                + totalAssetSimilarity * TOTAL_ASSET_WEIGHT
                + totalDebtSimilarity * TOTAL_DEBT_WEIGHT;

        // 백분율 값으로 변환 (소수점 둘째 자리까지)
        return Math.round(similarity * 10_000.0) / 100.0;
    }

    // 나이 차이 기준 유사도 계산
    private double calculateAgeSimilarity(
            int targetAge,
            int candidateAge
    ) {
        double ageDifference = Math.abs(targetAge - candidateAge);

        // 20살 이상 차이날 시 유사도 0
        double differenceRatio = Math.min(
                ageDifference / MAX_AGE_DIFFERENCE,
                1.0
        );

        return 1.0 - differenceRatio;
    }

    // 금액 기준 유사도 계산
    private double calculateAmountSimilarity(
            long targetAmount,
            long candidateAmount
    ) {
        // 음수 들어오면 0으로 처리
        double first = Math.max(targetAmount, 0L);
        double second = Math.max(candidateAmount, 0L);

        if (first == 0.0 && second == 0.0) {
            return 1.0;
        }

        double difference = Math.abs(first - second);
        double total = first + second;

        return 1.0 - (difference / total);
    }
}
