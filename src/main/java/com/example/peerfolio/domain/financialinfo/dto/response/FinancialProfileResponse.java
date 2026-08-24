package com.example.peerfolio.domain.financialinfo.dto.response;

import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "금융 프로필 응답")
public record FinancialProfileResponse(
		@Schema(description = "나이", example = "24")
		Integer age,

		@Schema(description = "월수입", example = "2500000")
		Long monthlyIncome,

		@Schema(description = "월 고정지출", example = "900000")
		Long fixedExpense,

		@Schema(description = "월 저축 목표", example = "700000")
		Long savingsGoal,

		@Schema(description = "총 보유자산", example = "12000000")
		Long totalAssetAmount,

		@Schema(description = "총 부채", example = "3000000")
		Long totalDebtAmount,

		@Schema(description = "순자산. 총 보유자산에서 총 부채를 뺀 계산값입니다.", example = "9000000")
		Long netAssetAmount
) {

	public static FinancialProfileResponse from(FinancialProfile financialProfile) {
		return new FinancialProfileResponse(
				financialProfile.getAge(),
				financialProfile.getMonthlyIncome(),
				financialProfile.getFixedExpense(),
				financialProfile.getSavingsGoal(),
				financialProfile.getTotalAssetAmount(),
				financialProfile.getTotalDebtAmount(),
				financialProfile.getTotalAssetAmount() - financialProfile.getTotalDebtAmount()
		);
	}
}
