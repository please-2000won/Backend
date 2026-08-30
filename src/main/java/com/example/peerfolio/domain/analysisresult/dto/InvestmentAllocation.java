package com.example.peerfolio.domain.analysisresult.dto;

public record InvestmentAllocation(
        double depositBondRatio,
        double domesticStockRatio,
        double foreignStockRatio,
        double alternativeRatio
) {
}
