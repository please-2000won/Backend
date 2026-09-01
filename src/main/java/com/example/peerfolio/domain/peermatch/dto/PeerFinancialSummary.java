package com.example.peerfolio.domain.peermatch.dto;

public record PeerFinancialSummary(
        String nickname,
        Long totalIncome,
        Long cash,
        InvestmentAssetSummary investmentAsset
) {
}
