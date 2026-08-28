package com.example.peerfolio.domain.peermatch.dto;

public record PeerFinancialData (
    Long userId,
    Integer age,
    Long monthlyIncome,
    Long fixedExpense,
    Long savingGoal,
    Long totalAssetAmount,
    Long totalDebtAmount,
    Long depositBondAmount,
    Long domesticStockAmount,
    Long foreignStockAmount,
    Long alternativeAmount
) {

}