package com.example.peerfolio.domain.financialinfo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "금융자산 요청")
public record FinancialAssetRequest(
		@Schema(description = "예금/적금/채권 금액", example = "6000000")
		@NotNull(message = "예금/적금/채권 금액은 필수입니다.")
		@PositiveOrZero(message = "예금/적금/채권 금액은 0 이상이어야 합니다.")
		@Max(value = 999999999999999L, message = "예금/적금/채권 금액은 999,999,999,999,999 이하여야 합니다.")
		Long depositBondAmount,

		@Schema(description = "국내주식 금액", example = "3000000")
		@NotNull(message = "국내주식 금액은 필수입니다.")
		@PositiveOrZero(message = "국내주식 금액은 0 이상이어야 합니다.")
		@Max(value = 999999999999999L, message = "국내주식 금액은 999,999,999,999,999 이하여야 합니다.")
		Long domesticStockAmount,

		@Schema(description = "해외주식 금액", example = "2000000")
		@NotNull(message = "해외주식 금액은 필수입니다.")
		@PositiveOrZero(message = "해외주식 금액은 0 이상이어야 합니다.")
		@Max(value = 999999999999999L, message = "해외주식 금액은 999,999,999,999,999 이하여야 합니다.")
		Long foreignStockAmount,

		@Schema(description = "대체/고위험자산 금액", example = "1000000")
		@NotNull(message = "대체/고위험자산 금액은 필수입니다.")
		@PositiveOrZero(message = "대체/고위험자산 금액은 0 이상이어야 합니다.")
		@Max(value = 999999999999999L, message = "대체/고위험자산 금액은 999,999,999,999,999 이하여야 합니다.")
		Long alternativeAmount
) {
}
