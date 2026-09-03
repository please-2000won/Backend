package com.example.peerfolio.domain.chatmessage.service;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;

public interface ChatAiClient {

	String generateAnswer(
			AnalysisResult analysisResult,
			FinancialProfile financialProfile,
			FinancialAsset financialAsset,
			String message
	);
}
