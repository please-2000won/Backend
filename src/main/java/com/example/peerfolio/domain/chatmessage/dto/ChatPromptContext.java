package com.example.peerfolio.domain.chatmessage.dto;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;

public record ChatPromptContext(
		Long analysisResultId,
		Integer peerCount,
		String benchmarkResult,
		String riskResult,
		Integer totalRiskScore,
		String analysisComment,
		Integer age,
		Long monthlyIncome,
		Long fixedExpense,
		Long savingsGoal,
		Long totalAssetAmount,
		Long totalDebtAmount,
		Long financialProfileId,
		Long financialAssetId,
		Long depositBondAmount,
		Long domesticStockAmount,
		Long foreignStockAmount,
		Long alternativeAmount
) {

	public static ChatPromptContext of(
			AnalysisResult analysisResult,
			FinancialProfile financialProfile,
			FinancialAsset financialAsset
	) {
		return new ChatPromptContext(
				analysisResult.getId(),
				analysisResult.getPeerCount(),
				analysisResult.getBenchmarkResult(),
				analysisResult.getRiskResult(),
				analysisResult.getTotalRiskScore(),
				analysisResult.getAnalysisComment(),
				financialProfile.getAge(),
				financialProfile.getMonthlyIncome(),
				financialProfile.getFixedExpense(),
				financialProfile.getSavingsGoal(),
				financialProfile.getTotalAssetAmount(),
				financialProfile.getTotalDebtAmount(),
				financialProfile.getId(),
				financialAsset.getId(),
				financialAsset.getDepositBondAmount(),
				financialAsset.getDomesticStockAmount(),
				financialAsset.getForeignStockAmount(),
				financialAsset.getAlternativeAmount()
		);
	}
}
