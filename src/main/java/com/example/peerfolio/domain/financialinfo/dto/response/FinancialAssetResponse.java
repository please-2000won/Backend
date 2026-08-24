package com.example.peerfolio.domain.financialinfo.dto.response;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "금융자산 응답")
public record FinancialAssetResponse(
		@Schema(description = "예금/적금/채권 금액", example = "6000000")
		Long depositBondAmount,

		@Schema(description = "국내주식 금액", example = "3000000")
		Long domesticStockAmount,

		@Schema(description = "해외주식 금액", example = "2000000")
		Long foreignStockAmount,

		@Schema(description = "대체/고위험자산 금액", example = "1000000")
		Long alternativeAmount,

		@Schema(description = "금융자산 합계. 예금/적금/채권, 국내주식, 해외주식, 대체/고위험자산의 합계입니다.", example = "12000000")
		Long totalFinancialAssetAmount
) {

	public static FinancialAssetResponse from(FinancialAsset financialAsset) {
		Long totalFinancialAssetAmount = financialAsset.getDepositBondAmount()
				+ financialAsset.getDomesticStockAmount()
				+ financialAsset.getForeignStockAmount()
				+ financialAsset.getAlternativeAmount();

		return new FinancialAssetResponse(
				financialAsset.getDepositBondAmount(),
				financialAsset.getDomesticStockAmount(),
				financialAsset.getForeignStockAmount(),
				financialAsset.getAlternativeAmount(),
				totalFinancialAssetAmount
		);
	}
}
