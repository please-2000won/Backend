package com.example.peerfolio.domain.analysisresult.dto;

public record InvestmentBenchmark (
    int peerCount,

    long averageDepositBondAmount,
    long averageDomesticStockAmount,
    long averageForeignStockAmount,
    long averageAlternativeAmount,

    double averageDepositBondRatio,
    double averageDomesticStockRatio,
    double averageForeignStockRatio,
    double averageAlternativeRatio
) {
}
