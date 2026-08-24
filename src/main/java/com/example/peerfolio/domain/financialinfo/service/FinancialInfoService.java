package com.example.peerfolio.domain.financialinfo.service;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialinfo.dto.request.FinancialAssetRequest;
import com.example.peerfolio.domain.financialinfo.dto.request.FinancialInfoRequest;
import com.example.peerfolio.domain.financialinfo.dto.request.FinancialProfileRequest;
import com.example.peerfolio.domain.financialinfo.dto.response.FinancialInfoResponse;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialInfoService {

	private final FinancialProfileRepository financialProfileRepository;
	private final FinancialAssetRepository financialAssetRepository;

	public FinancialInfoResponse getMyFinancialInfo(User user) {
		FinancialProfile financialProfile = financialProfileRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));
		FinancialAsset financialAsset = financialAssetRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));

		return FinancialInfoResponse.of(financialProfile, financialAsset);
	}

	@Transactional
	public FinancialInfoResponse upsertMyFinancialInfo(
			User user,
			FinancialInfoRequest request
	) {
		FinancialProfile financialProfile = upsertFinancialProfile(user, request.financialProfile());
		FinancialAsset financialAsset = upsertFinancialAsset(user, request.financialAsset());

		return FinancialInfoResponse.of(financialProfile, financialAsset);
	}

	private FinancialProfile upsertFinancialProfile(
			User user,
			FinancialProfileRequest request
	) {
		return financialProfileRepository.findByUserId(user.getId())
				.map(financialProfile -> {
					financialProfile.update(
							request.age(),
							request.monthlyIncome(),
							request.fixedExpense(),
							request.savingsGoal(),
							request.totalAssetAmount(),
							request.totalDebtAmount()
					);
					return financialProfile;
				})
				.orElseGet(() -> financialProfileRepository.save(FinancialProfile.create(
						user,
						request.age(),
						request.monthlyIncome(),
						request.fixedExpense(),
						request.savingsGoal(),
						request.totalAssetAmount(),
						request.totalDebtAmount()
				)));
	}

	private FinancialAsset upsertFinancialAsset(
			User user,
			FinancialAssetRequest request
	) {
		return financialAssetRepository.findByUserId(user.getId())
				.map(financialAsset -> {
					financialAsset.update(
							request.depositBondAmount(),
							request.domesticStockAmount(),
							request.foreignStockAmount(),
							request.alternativeAmount()
					);
					return financialAsset;
				})
				.orElseGet(() -> financialAssetRepository.save(FinancialAsset.create(
						user,
						request.depositBondAmount(),
						request.domesticStockAmount(),
						request.foreignStockAmount(),
						request.alternativeAmount()
				)));
	}
}
