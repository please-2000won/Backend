package com.example.peerfolio.domain.peermatch.dto;

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
}
