package com.example.peerfolio.domain.financialinfo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "내 금융 정보 저장 요청")
public record FinancialInfoRequest(
		@Schema(description = "금융 프로필")
		@Valid
		@NotNull(message = "금융 프로필 정보는 필수입니다.")
		FinancialProfileRequest financialProfile,

		@Schema(description = "금융자산")
		@Valid
		@NotNull(message = "금융자산 정보는 필수입니다.")
		FinancialAssetRequest financialAsset
) {
}
