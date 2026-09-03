package com.example.peerfolio.domain.chatmessage.service;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.chatmessage.code.ChatErrorCode;
import com.example.peerfolio.domain.chatmessage.dto.ChatPromptContext;
import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatContextQueryService {

	private final AnalysisResultRepository analysisResultRepository;
	private final FinancialProfileRepository financialProfileRepository;
	private final FinancialAssetRepository financialAssetRepository;

	public ChatPromptContext getPromptContext(Long userId) {
		AnalysisResult analysisResult =
				analysisResultRepository.findByUserId(userId)
						.orElseThrow(() ->
								new ProjectException(
										ChatErrorCode.ANALYSIS_NOT_FOUND
								)
						);

		FinancialProfile financialProfile =
				financialProfileRepository.findByUserId(userId)
						.orElseThrow(() ->
								new ProjectException(
										GeneralErrorCode.NOT_FOUND
								)
						);

		FinancialAsset financialAsset =
				financialAssetRepository.findByUserId(userId)
						.orElseThrow(() ->
								new ProjectException(
										GeneralErrorCode.NOT_FOUND
								)
						);

		return ChatPromptContext.of(
				analysisResult,
				financialProfile,
				financialAsset
		);
	}
}
