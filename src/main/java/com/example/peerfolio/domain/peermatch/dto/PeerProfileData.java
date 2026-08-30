package com.example.peerfolio.domain.peermatch.dto;

public record PeerProfileData (
    Long userId,
    Integer age,
    Long monthlyIncome,
    Long fixedExpense,
    Long savingsGoal,
    Long totalAssetAmount,
    Long totalDebtAmount
) {

}
