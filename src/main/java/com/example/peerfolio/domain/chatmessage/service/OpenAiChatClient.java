package com.example.peerfolio.domain.chatmessage.service;

import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.analysisresult.dto.PeerProfileBenchmark;
import com.example.peerfolio.domain.chatmessage.dto.ChatPromptContext;
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
			ChatPromptContext context,
			String message
	) {
		OpenAiRequest request = OpenAiRequest.create(
				properties.model(),
				INSTRUCTIONS,
				createInput(
						context,
						message
				),
				SCHEMA_NAME,
				RESPONSE_SCHEMA
		);

		String responseText = openAiClient.createResponse(request);
		return parseAnswer(responseText);
	}

	private String createInput(
			ChatPromptContext context,
			String message
	) {
		BenchmarkResult benchmarkResult =
				parseBenchmarkResult(context);
		AnalysisResponse.RiskResult riskResult =
				parseRiskResult(context);
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
				context.age(),
				context.monthlyIncome(),
				context.fixedExpense(),
				context.savingsGoal(),
				context.totalAssetAmount(),
				context.totalDebtAmount(),
				context.depositBondAmount(),
				formatRatio(context.depositBondAmount(), context),
				context.domesticStockAmount(),
				formatRatio(context.domesticStockAmount(), context),
				context.foreignStockAmount(),
				formatRatio(context.foreignStockAmount(), context),
				context.alternativeAmount(),
				formatRatio(context.alternativeAmount(), context),
				context.analysisResultId(),
				context.peerCount(),
				context.totalRiskScore(),
				riskResult.riskLevel(),
				riskResult.summary(),
				context.analysisComment(),
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
			ChatPromptContext context
	) {
		try {
			return objectMapper.readValue(
					context.benchmarkResult(),
					BenchmarkResult.class
			);
		} catch (JacksonException e) {
			throw new ProjectException(
					GeneralErrorCode.INTERNAL_SERVER_ERROR
			);
		}
	}

	private AnalysisResponse.RiskResult parseRiskResult(
			ChatPromptContext context
	) {
		try {
			return objectMapper.readValue(
					context.riskResult(),
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
			ChatPromptContext context
	) {
		long total = context.depositBondAmount()
				+ context.domesticStockAmount()
				+ context.foreignStockAmount()
				+ context.alternativeAmount();

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
