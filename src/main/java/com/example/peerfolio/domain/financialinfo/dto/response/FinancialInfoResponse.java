package com.example.peerfolio.domain.financialinfo.dto.response;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 금융 정보 응답")
public record FinancialInfoResponse(
		@Schema(description = "금융 프로필")
		FinancialProfileResponse financialProfile,

		@Schema(description = "금융자산")
		FinancialAssetResponse financialAsset
) {

	public static FinancialInfoResponse of(
			FinancialProfile financialProfile,
			FinancialAsset financialAsset
	) {
		return new FinancialInfoResponse(
				FinancialProfileResponse.from(financialProfile),
				FinancialAssetResponse.from(financialAsset)
		);
	}
}
