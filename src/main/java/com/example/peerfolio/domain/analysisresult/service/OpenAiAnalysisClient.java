package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import com.example.peerfolio.global.openai.client.OpenAiClient;
import com.example.peerfolio.global.openai.code.OpenAiErrorCode;
import com.example.peerfolio.global.openai.config.OpenAiProperties;
import com.example.peerfolio.global.openai.dto.OpenAiAnalysisPayload;
import com.example.peerfolio.global.openai.dto.OpenAiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OpenAiAnalysisClient implements AiAnalysisClient {

    private static final Set<String> RISK_LEVELS =
            Set.of("LOW", "MEDIUM", "HIGH");

    private static final String INSTRUCTIONS = """
        You are a financial analysis assistant that compares a user's
        financial condition with anonymized peer-group averages.

        Use only the data provided in the input.
        Do not infer personal information or financial information
        that is not explicitly provided.
        Do not analyze individual peer users.
        Use only aggregated peer-group averages as the comparison baseline.

        Do not recommend buying, selling, or investing in any specific
        financial product.
        Do not guarantee returns or present the result as professional
        financial advice.
        Focus on providing an objective and understandable comparison.

        [Economic condition comparison]

        Compare the user's monthly income and total assets with the
        peer-group averages.

        Also consider the user's fixed expenses, savings goal, and debt
        when evaluating the user's overall financial condition.

        Do not merely state that an amount is higher or lower.
        Explain the user's fixed-expense and savings burden relative
        to monthly income, as well as the debt burden relative to total assets.

        [Investment asset comparison]

        Compare investment assets using both absolute amounts and
        allocation ratios within the user's total investment assets.

        Calculate the allocation difference as follows:

        allocation difference =
        user allocation ratio - peer-group average allocation ratio

        Use the absolute value of the allocation difference to determine
        the size of the difference.
        Use the sign of the difference to determine whether the user's
        allocation is higher or lower than the peer-group average.

        Describe allocation differences using these thresholds:

        - 0 to 5 percentage points:
          similar to the peer-group average
        - More than 5 to 15 percentage points:
          slightly higher or slightly lower than the peer-group average
        - More than 15 to 30 percentage points:
          higher or lower than the peer-group average
        - More than 30 percentage points:
          significantly higher or significantly lower than the peer-group average

        [Risk assessment]

        Do not determine that an allocation is risky merely because
        it differs from the peer-group average.

        Consider both the characteristics of each asset category
        and the level of concentration in a single asset category.

        A deposit-and-bond allocation above the peer-group average
        must not by itself increase the risk assessment.

        A significantly lower deposit-and-bond allocation may indicate
        insufficient defensive asset allocation.

        Domestic or foreign stock allocations significantly above
        the peer-group averages may indicate relatively higher exposure
        to market volatility.

        An alternative and high-risk asset allocation above the
        peer-group average must have a stronger effect on the risk
        assessment than the same difference in other asset categories.

        If any single investment asset category exceeds 50 percent
        of the user's total investment assets, identify possible
        concentration risk.

        If any single investment asset category exceeds 70 percent,
        identify high concentration risk.

        If the user's total investment assets are zero:
        - Treat every user investment allocation ratio as zero.
        - Treat investment concentration risk as zero.
        - Do not claim that the absence of investment assets itself
          represents high investment risk.

        [Risk score]

        Generate a total risk score from 0 to 100.
        A lower score means lower risk.
        A higher score means higher risk.

        Use the following risk levels:

        - LOW: 0 to 33
        - MEDIUM: 34 to 66
        - HIGH: 67 to 100

        The risk level must always be consistent with the total risk score.

        [Analysis text]

        Prioritize the financial categories with the largest meaningful
        differences between the user and the peer group.

        Clearly state which investment asset allocations are higher
        or lower than the peer-group averages.

        Do not merely list numbers.
        Explain what the differences mean for the user's financial
        asset composition and relative risk exposure.

        You may briefly mention categories that are similar to the
        peer-group averages when relevant.

        Avoid excessively negative, alarming, or judgmental language.
        Maintain the position that the result is reference information
        for comparison and not investment advice.

        Write riskResult.summary and analysisComment in natural Korean.
        Write all user-facing content in Korean.

        Make analysisComment clear and easy to understand.
        Limit analysisComment to no more than five Korean sentences.
        """;

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public AiAnalysisResult analyzePeerBenchmark(
            PeerProfileData targetProfile,
            PeerAssetData targetAsset,
            BenchmarkResult benchmarkResult
    ) {
        // 사용자 데이터와 익명화된 피어 그룹 평균만 JSON으로 변환
        String input = createInput(
                targetProfile,
                targetAsset,
                benchmarkResult
        );

        OpenAiRequest request = OpenAiRequest.create(
                properties.model(),
                INSTRUCTIONS,
                input,
                OpenAiAnalysisSchema.create()
        );

        String responseText = openAiClient.createResponse(request);

        return parseAnalysisResult(responseText);
    }

    private String createInput(
            PeerProfileData targetProfile,
            PeerAssetData targetAsset,
            BenchmarkResult benchmarkResult
    ) {
        // userId는 OpenAI에 전달하지 않음
        Map<String, Object> userProfile = Map.of(
                "age", targetProfile.age(),
                "monthlyIncome", targetProfile.monthlyIncome(),
                "fixedExpense", targetProfile.fixedExpense(),
                "savingsGoal", targetProfile.savingsGoal(),
                "totalAssetAmount", targetProfile.totalAssetAmount(),
                "totalDebtAmount", targetProfile.totalDebtAmount()
        );

        Map<String, Object> userAsset = Map.of(
                "depositBondAmount", targetAsset.depositBondAmount(),
                "domesticStockAmount", targetAsset.domesticStockAmount(),
                "foreignStockAmount", targetAsset.foreignStockAmount(),
                "alternativeAmount", targetAsset.alternativeAmount()
        );

        Map<String, Object> peerProfile = Map.of(
                "averageMonthlyIncome",
                benchmarkResult.profile().averageMonthlyIncome(),
                "averageTotalAssetAmount",
                benchmarkResult.profile().averageTotalAssetAmount()
        );

        Map<String, Object> peerInvestment = Map.of(
                "peerCount",
                benchmarkResult.investment().peerCount(),
                "averageDepositBondAmount",
                benchmarkResult.investment().averageDepositBondAmount(),
                "averageDomesticStockAmount",
                benchmarkResult.investment().averageDomesticStockAmount(),
                "averageForeignStockAmount",
                benchmarkResult.investment().averageForeignStockAmount(),
                "averageAlternativeAmount",
                benchmarkResult.investment().averageAlternativeAmount(),
                "averageDepositBondRatio",
                benchmarkResult.investment().averageDepositBondRatio(),
                "averageDomesticStockRatio",
                benchmarkResult.investment().averageDomesticStockRatio(),
                "averageForeignStockRatio",
                benchmarkResult.investment().averageForeignStockRatio(),
                "averageAlternativeRatio",
                benchmarkResult.investment().averageAlternativeRatio()
        );

        Map<String, Object> input = Map.of(
                "user", Map.of(
                        "financialProfile", userProfile,
                        "investmentProfile", userAsset
                ),
                "peerGroup", Map.of(
                        "profileBenchmark", peerProfile,
                        "investmentBenchmark", peerInvestment
                )
        );

        try {
            return objectMapper.writeValueAsString(input);
        } catch (JacksonException e) {
            throw new ProjectException(
                    OpenAiErrorCode.REQUEST_FAILED
            );
        }
    }

    private AiAnalysisResult parseAnalysisResult(String responseText) {
        try {
            OpenAiAnalysisPayload payload =
                    objectMapper.readValue(responseText, OpenAiAnalysisPayload.class);

            validate(payload);

            // riskResult 객체는 DB JSON 컬럼에 저장할 문자열로 변환
            String riskResultJson =
                    objectMapper.writeValueAsString(
                            payload.riskResult()
                    );

            return new AiAnalysisResult(
                    riskResultJson,
                    payload.totalRiskScore(),
                    payload.analysisComment()
            );
        } catch (JacksonException e) {
            throw new ProjectException(
                    OpenAiErrorCode.INVALID_RESPONSE
            );
        }
    }

    private void validate(OpenAiAnalysisPayload payload) {
        if (payload == null
                || payload.riskResult() == null
                || payload.totalRiskScore() == null
                || payload.analysisComment() == null
                || payload.analysisComment().isBlank()
                || payload.riskResult().riskLevel() == null
                ||  payload.riskResult().summary() == null
                || payload.riskResult().summary().isBlank()) {
            throw new ProjectException(
                    OpenAiErrorCode.INVALID_RESPONSE
            );
        }

        int score = payload.totalRiskScore();

        if (score < 0 || score > 100) {
            throw new ProjectException(
                    OpenAiErrorCode.INVALID_RESPONSE
            );
        }

        String riskLevel = payload.riskResult().riskLevel();

        if (!RISK_LEVELS.contains(riskLevel)
                || !isConsistentRiskLevel(riskLevel, score)) {
            throw new ProjectException(
                    OpenAiErrorCode.INVALID_RESPONSE
            );
        }
    }

    private boolean isConsistentRiskLevel(String riskLevel, int score) {
        return switch (riskLevel) {
            case "LOW" -> score <= 33;
            case "MEDIUM" -> score >= 34 && score <= 66;
            case "HIGH" -> score >= 67;
            default -> false;
        };
    }
}
