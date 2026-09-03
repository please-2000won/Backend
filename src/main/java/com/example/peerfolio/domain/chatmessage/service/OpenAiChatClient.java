package com.example.peerfolio.domain.chatmessage.service;

import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.analysisresult.dto.PeerProfileBenchmark;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import com.example.peerfolio.global.openai.client.OpenAiClient;
import com.example.peerfolio.global.openai.code.OpenAiErrorCode;
import com.example.peerfolio.global.openai.config.OpenAiProperties;
import com.example.peerfolio.global.openai.dto.OpenAiRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OpenAiChatClient implements ChatAiClient {

	private static final String SCHEMA_NAME = "peerfolio_chat_response";

	private static final String INSTRUCTIONS = """
			You are Peerfolio's financial coaching chatbot.
			Always respond in Korean.
			Base your responses only on the user's financial profile, asset data, and saved peer analysis summary. Do not infer, assume, or fabricate any financial circumstances or figures that are not provided. If there is not enough information to answer the user's question, clearly state that the available information is insufficient.
			Do not claim certainty about future investment returns or performance, and do not guarantee any financial outcome. Do not recommend specific securities, tickers, or investments presented as guaranteed to generate returns.
			Keep your responses practical, concise, and focused on actions the user can realistically take. Limit each response to 5 sentences or fewer. When a list is necessary, use no more than 3 bullet points.
			If the user's question falls outside personal finance or the provided analysis context, briefly explain that it cannot be answered based on the available information and provide only safe, general guidance.
			""";

	private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
			"type", "object",
			"additionalProperties", false,
			"properties", Map.of(
					"answer", Map.of(
							"type", "string",
							"minLength", 1,
							"maxLength", 1200
					)
			),
			"required", List.of("answer")
	);

	private final OpenAiClient openAiClient;
	private final OpenAiProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public String generateAnswer(
			AnalysisResult analysisResult,
			FinancialProfile financialProfile,
			FinancialAsset financialAsset,
			String message
	) {
		OpenAiRequest request = OpenAiRequest.create(
				properties.model(),
				INSTRUCTIONS,
				createInput(
						analysisResult,
						financialProfile,
						financialAsset,
						message
				),
				SCHEMA_NAME,
				RESPONSE_SCHEMA
		);

		String responseText = openAiClient.createResponse(request);
		return parseAnswer(responseText);
	}

	private String createInput(
			AnalysisResult analysisResult,
			FinancialProfile financialProfile,
			FinancialAsset financialAsset,
			String message
	) {
		BenchmarkResult benchmarkResult =
				parseBenchmarkResult(analysisResult);
		AnalysisResponse.RiskResult riskResult =
				parseRiskResult(analysisResult);
		InvestmentBenchmark investmentBenchmark =
				benchmarkResult.investment();
		PeerProfileBenchmark profileBenchmark =
				benchmarkResult.profile();

		return """
				[User Financial Profile]
				age: %d
				monthlyIncome: %d
				fixedExpense: %d
				savingsGoal: %d
				totalAssetAmount: %d
				totalDebtAmount: %d

				[User Asset Allocation]
				depositBondAmount: %d (%s%%)
				domesticStockAmount: %d (%s%%)
				foreignStockAmount: %d (%s%%)
				alternativeAmount: %d (%s%%)

				[Peer Analysis Summary]
				analysisResultId: %d
				peerCount: %d
				totalRiskScore: %d
				riskLevel: %s
				riskSummary: %s
				analysisComment: %s

				[Peer Average Summary]
				averageMonthlyIncome: %d
				averageTotalAssetAmount: %d
				averageDepositBondRatio: %s%%
				averageDomesticStockRatio: %s%%
				averageForeignStockRatio: %s%%
				averageAlternativeRatio: %s%%

				[Current Question]
				%s
				""".formatted(
				financialProfile.getAge(),
				financialProfile.getMonthlyIncome(),
				financialProfile.getFixedExpense(),
				financialProfile.getSavingsGoal(),
				financialProfile.getTotalAssetAmount(),
				financialProfile.getTotalDebtAmount(),
				financialAsset.getDepositBondAmount(),
				formatRatio(financialAsset.getDepositBondAmount(), financialAsset),
				financialAsset.getDomesticStockAmount(),
				formatRatio(financialAsset.getDomesticStockAmount(), financialAsset),
				financialAsset.getForeignStockAmount(),
				formatRatio(financialAsset.getForeignStockAmount(), financialAsset),
				financialAsset.getAlternativeAmount(),
				formatRatio(financialAsset.getAlternativeAmount(), financialAsset),
				analysisResult.getId(),
				analysisResult.getPeerCount(),
				analysisResult.getTotalRiskScore(),
				riskResult.riskLevel(),
				riskResult.summary(),
				analysisResult.getAnalysisComment(),
				profileBenchmark.averageMonthlyIncome(),
				profileBenchmark.averageTotalAssetAmount(),
				formatRatio(investmentBenchmark.averageDepositBondRatio()),
				formatRatio(investmentBenchmark.averageDomesticStockRatio()),
				formatRatio(investmentBenchmark.averageForeignStockRatio()),
				formatRatio(investmentBenchmark.averageAlternativeRatio()),
				message
		);
	}

	private BenchmarkResult parseBenchmarkResult(
			AnalysisResult analysisResult
	) {
		try {
			return objectMapper.readValue(
					analysisResult.getBenchmarkResult(),
					BenchmarkResult.class
			);
		} catch (JacksonException e) {
			throw new ProjectException(
					GeneralErrorCode.INTERNAL_SERVER_ERROR
			);
		}
	}

	private AnalysisResponse.RiskResult parseRiskResult(
			AnalysisResult analysisResult
	) {
		try {
			return objectMapper.readValue(
					analysisResult.getRiskResult(),
					AnalysisResponse.RiskResult.class
			);
		} catch (JacksonException e) {
			throw new ProjectException(
					GeneralErrorCode.INTERNAL_SERVER_ERROR
			);
		}
	}

	private String parseAnswer(String responseText) {
		try {
			ChatAiResponse response = objectMapper.readValue(
					responseText,
					ChatAiResponse.class
			);

			if (response.answer() == null
					|| response.answer().isBlank()) {
				throw new ProjectException(
						OpenAiErrorCode.INVALID_RESPONSE
				);
			}

			return response.answer();
		} catch (JacksonException e) {
			throw new ProjectException(
					OpenAiErrorCode.INVALID_RESPONSE
			);
		}
	}

	private String formatRatio(
			Long amount,
			FinancialAsset financialAsset
	) {
		long total = financialAsset.getDepositBondAmount()
				+ financialAsset.getDomesticStockAmount()
				+ financialAsset.getForeignStockAmount()
				+ financialAsset.getAlternativeAmount();

		if (total <= 0) {
			return "0.0";
		}

		return formatRatio(amount * 100.0 / total);
	}

	private String formatRatio(double ratio) {
		return "%.1f".formatted(ratio);
	}

	private record ChatAiResponse(String answer) {
	}
}
