package com.example.peerfolio.domain.peermatch.dto;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;

public record PeerFinancialSummary(
        String nickname,
        Long totalIncome,
        Long cash,
        InvestmentAssetSummary investmentAsset
) {

    public static PeerFinancialSummary of(
            String nickname,
            FinancialProfile profile,
            FinancialAsset asset
    ) {
        long totalIncome =
                profile.getMonthlyIncome()
                - profile.getFixedExpense()
                - profile.getSavingsGoal();

        long cash =
                profile.getTotalAssetAmount()
                -profile.getTotalDebtAmount();

        return new PeerFinancialSummary(
                nickname,
                totalIncome,
                cash,
                InvestmentAssetSummary.from(asset)
        );
    }
}
