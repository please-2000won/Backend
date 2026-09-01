package com.example.peerfolio.domain.peermatch.dto;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;

public record InvestmentAssetSummary(
        Long depositBondAmount,
        Long domesticStockAmount,
        Long foreignStockAmount,
        Long alternativeAmount,

        Double depositBondRatio,
        Double domesticStockRatio,
        Double foreignStockRatio,
        Double alternativeRatio
) {

    public static InvestmentAssetSummary from(
            FinancialAsset asset
    ) {
        long totalAmount =
                asset.getDepositBondAmount()
                + asset.getDomesticStockAmount()
                + asset.getForeignStockAmount()
                + asset.getAlternativeAmount();

        return new InvestmentAssetSummary(
                asset.getDepositBondAmount(),
                asset.getDomesticStockAmount(),
                asset.getForeignStockAmount(),
                asset.getAlternativeAmount(),
                calculateRatio(
                        asset.getDepositBondAmount(),
                        totalAmount
                ),
                calculateRatio(
                        asset.getDomesticStockAmount(),
                        totalAmount
                ),
                calculateRatio(
                        asset.getForeignStockAmount(),
                        totalAmount
                ),
                calculateRatio(
                        asset.getAlternativeAmount(),
                        totalAmount
                )
        );
    }

    private static double calculateRatio(
            long amount,
            long totalAmount
    ) {
        if (totalAmount <= 0) {
            return 0.0;
        }

        double ratio = (double) amount / totalAmount * 100.0;

        return Math.round(ratio * 100.0) / 100.0;
    }
}
