package com.example.peerfolio.domain.financialinfo.dto.response;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "금융 정보 응답")
public record FinancialInfoResponse(
		@Schema(description = "금융 프로필")
		FinancialProfileResponse financialProfile,

		@Schema(description = "금융 자산")
		FinancialAssetResponse financialAsset,

		@Schema(description = "금융 정보 최종 수정 시각", example = "2026-09-04T12:30:00")
		LocalDateTime updatedAt
) {

	public static FinancialInfoResponse of(
			FinancialProfile financialProfile,
			FinancialAsset financialAsset
	) {
		return new FinancialInfoResponse(
				FinancialProfileResponse.from(financialProfile),
				FinancialAssetResponse.from(financialAsset),
				latestUpdatedAt(
						financialProfile.getUpdatedAt(),
						financialAsset.getUpdatedAt()
				)
		);
	}

	private static LocalDateTime latestUpdatedAt(
			LocalDateTime profileUpdatedAt,
			LocalDateTime assetUpdatedAt
	) {
		if (profileUpdatedAt == null) {
			return assetUpdatedAt;
		}

		if (assetUpdatedAt == null) {
			return profileUpdatedAt;
		}

		if (profileUpdatedAt.isAfter(assetUpdatedAt)) {
			return profileUpdatedAt;
		}

		return assetUpdatedAt;
	}
}
