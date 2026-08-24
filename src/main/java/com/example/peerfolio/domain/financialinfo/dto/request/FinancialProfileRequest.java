package com.example.peerfolio.domain.financialinfo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "금융 프로필 요청")
public record FinancialProfileRequest(
		@Schema(description = "나이. 나이대는 저장하지 않고 추후 매칭/AI 분석에서 age 기준으로 계산합니다.", example = "24")
		@NotNull(message = "나이는 필수입니다.")
		@Positive(message = "나이는 0보다 커야 합니다.")
		@Max(value = 120, message = "나이는 120 이하여야 합니다.")
		Integer age,

		@Schema(description = "월수입", example = "2500000")
		@NotNull(message = "월수입은 필수입니다.")
		@PositiveOrZero(message = "월수입은 0 이상이어야 합니다.")
		Long monthlyIncome,

		@Schema(description = "월 고정지출", example = "900000")
		@NotNull(message = "월 고정지출은 필수입니다.")
		@PositiveOrZero(message = "월 고정지출은 0 이상이어야 합니다.")
		Long fixedExpense,

		@Schema(description = "월 저축 목표", example = "700000")
		@NotNull(message = "월 저축 목표는 필수입니다.")
		@PositiveOrZero(message = "월 저축 목표는 0 이상이어야 합니다.")
		Long savingsGoal,

		@Schema(description = "총 보유자산", example = "12000000")
		@NotNull(message = "총 보유자산은 필수입니다.")
		@PositiveOrZero(message = "총 보유자산은 0 이상이어야 합니다.")
		Long totalAssetAmount,

		@Schema(description = "총 부채", example = "3000000")
		@NotNull(message = "총 부채는 필수입니다.")
		@PositiveOrZero(message = "총 부채는 0 이상이어야 합니다.")
		Long totalDebtAmount
) {
}
