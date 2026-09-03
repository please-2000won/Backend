package com.example.peerfolio.domain.analysisresult.ai;

import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentAllocation;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.analysisresult.calculator.InvestmentBenchmarkCalculator;
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
        You are a financial analysis assistant that evaluates a user's financial condition
        by comparing it with the aggregated averages of an anonymized peer group.

        Use only the provided data.
        Do not infer any personal or financial information that has not been provided.
        Do not analyze individual peers.
        Use only aggregated peer-group averages as the comparison target.

        Do not directly recommend buying, selling, or investing in any particular financial product.
        Do not guarantee returns or present the result as a definitive professional financial diagnosis.
        Focus on objectively comparing and explaining the user's financial condition.

        [Economic Condition Comparison]

        Compare the user's monthly income and cash-equivalent assets with the peer-group averages.
        Explain the burden of fixed expenses and savings relative to income,
        as well as the debt burden relative to cash-equivalent assets.

        The `totalAssetAmount` field represents the user's cash and cash-equivalent holdings.
        It does not include investment assets.

        Investment assets, including deposits and bonds, domestic stocks, foreign stocks,
        and alternative or high-risk assets, are managed separately from `totalAssetAmount`.

        Do not compare `totalAssetAmount` with the sum of investment assets
        to validate data consistency.
        Never describe a difference between those values as a data inconsistency.

        If monthly income is zero, do not calculate fixed-expense or savings ratios.
        State only, in neutral language, that the user currently has no monthly income.

        If cash-equivalent assets are zero, do not calculate a debt ratio.
        Explain only whether the user holds debt without cash-equivalent assets.

        [Investment Asset Comparison]

        Compare investment assets using both their absolute amounts
        and their allocation percentages within total investment assets.

        Calculate the allocation difference using the following formula:

        Allocation difference =
        User allocation percentage - Peer-group average allocation percentage

        Determine the magnitude of the difference using its absolute value.
        If the result is positive, describe the allocation as higher.
        If the result is negative, describe the allocation as lower.

        - Absolute difference from 0 to 5 percentage points: similar
        - Absolute difference greater than 5 and up to 15 percentage points:
          somewhat higher or somewhat lower
        - Absolute difference greater than 15 and up to 30 percentage points:
          higher or lower
        - Absolute difference greater than 30 percentage points:
          significantly higher or significantly lower

        If the user's total investment assets are zero,
        treat all investment allocation percentages as zero
        and assign no risk based on investment concentration.

        [Risk Assessment]

        Do not consider the user risky solely because their asset allocation
        differs from the peer-group average.

        Consider the characteristics of each asset type
        and the degree of concentration in a particular asset type.

        Do not increase the risk assessment solely because the user's
        deposits-and-bonds allocation is higher than the peer-group average.

        If the deposits-and-bonds allocation is significantly lower,
        it may be interpreted as a relatively limited defensive-asset allocation.

        If domestic-stock or foreign-stock allocation is significantly higher
        than the peer-group average,
        interpret it as relatively greater exposure to market volatility.

        If alternative or high-risk asset allocation is higher than the peer-group average,
        give it greater weight in the risk assessment than other asset types.

        If domestic stocks, foreign stocks, or alternative and high-risk assets
        account for more than 50% of total investment assets,
        identify a possibility of asset concentration.

        If such an allocation exceeds 70%,
        identify a high asset-concentration risk.

        Do not apply these concentration thresholds to deposits and bonds.
        Even if deposits and bonds exceed 50% or 70%,
        do not increase the risk score solely for that reason.

        You may provide a neutral explanation concerning liquidity or diversification.

        Do not assign a higher risk level solely because the user is young
        or because their monthly income, cash-equivalent assets,
        or investment assets are small in absolute terms.

        Use economic conditions to explain comparisons with the peer group.

        Consider debt burden,
        fixed expenses relative to income,
        the risk characteristics of investment asset types,
        and investment concentration when assessing risk.

        [Risk Score]

        Generate an overall risk score from 0 to 100.
        A lower score indicates lower risk,
        while a higher score indicates higher risk.

        Do not convert differences from peer-group averages directly into risk points.

        Prioritize the user's absolute investment concentration
        and the risk characteristics of each asset type.

        Use debt burden and fixed expenses relative to income as secondary factors.

        Use the following risk levels:

        - LOW: 0 to 33
        - MEDIUM: 34 to 66
        - HIGH: 67 to 100

        The risk level and overall risk score must always be consistent.

        [Analysis Text]

        Prioritize the financial items with the greatest differences
        between the user and the peer group.

        Clearly state which investment allocation percentages
        are higher or lower than the peer-group averages.

        Do not merely list numbers.
        Explain what those differences mean for the user's financial asset composition
        and risk exposure.

        You may briefly mention items similar to the peer-group averages when relevant.
        Avoid excessively negative or anxiety-inducing language.

        Maintain the perspective that the analysis is comparative reference information,
        not investment advice.

        Use neutral expressions such as "may be worth reviewing" or "can be considered,"
        rather than directive expressions such as "recommend," "should invest," or "must change."

        Write `riskResult.summary` and `analysisComment` in natural Korean.

        Divide `analysisComment` into exactly three paragraphs:

        First paragraph:
        Compare the user's economic condition with the peer-group averages.

        Second paragraph:
        Compare investment asset amounts and allocation percentages.

        Third paragraph:
        Summarize the primary risk factors and mitigating factors.

        Write each paragraph as natural prose without headings or bullet points.
        Separate each paragraph with exactly one blank line
        by using two newline characters (`\\n\\n`).

        Limit the entire `analysisComment` to no more than five Korean sentences.
        """;

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final InvestmentBenchmarkCalculator investmentBenchmarkCalculator;

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
        // 분석 대상 사용자의 투자자산 구성 비율
        InvestmentAllocation userAllocation =
                investmentBenchmarkCalculator.calculateAllocation(
                        targetAsset
                );

        InvestmentBenchmark peerInvestment =
                benchmarkResult.investment();

        // userId 등 식별정보는 OpenAI에 전달하지 않는다.
        Map<String, Object> userProfile = Map.of(
                "age", targetProfile.age(),
                "monthlyIncome", targetProfile.monthlyIncome(),
                "fixedExpense", targetProfile.fixedExpense(),
                "savingsGoal", targetProfile.savingsGoal(),
                "totalAssetAmount", targetProfile.totalAssetAmount(),
                "totalDebtAmount", targetProfile.totalDebtAmount()
        );

        Map<String, Object> userAssetAmounts = Map.of(
                "depositBondAmount",
                targetAsset.depositBondAmount(),
                "domesticStockAmount",
                targetAsset.domesticStockAmount(),
                "foreignStockAmount",
                targetAsset.foreignStockAmount(),
                "alternativeAmount",
                targetAsset.alternativeAmount()
        );

        Map<String, Object> userAssetRatios = Map.of(
                "depositBondRatio",
                userAllocation.depositBondRatio(),
                "domesticStockRatio",
                userAllocation.domesticStockRatio(),
                "foreignStockRatio",
                userAllocation.foreignStockRatio(),
                "alternativeRatio",
                userAllocation.alternativeRatio()
        );

        Map<String, Object> userAsset = Map.of(
                "amounts", userAssetAmounts,
                "allocationRatios", userAssetRatios
        );

        Map<String, Object> peerProfile = Map.of(
                "averageMonthlyIncome",
                benchmarkResult.profile().averageMonthlyIncome(),
                "averageTotalAssetAmount",
                benchmarkResult.profile().averageTotalAssetAmount()
        );

        Map<String, Object> peerAssetAmounts = Map.of(
                "averageDepositBondAmount",
                peerInvestment.averageDepositBondAmount(),
                "averageDomesticStockAmount",
                peerInvestment.averageDomesticStockAmount(),
                "averageForeignStockAmount",
                peerInvestment.averageForeignStockAmount(),
                "averageAlternativeAmount",
                peerInvestment.averageAlternativeAmount()
        );

        Map<String, Object> peerAssetRatios = Map.of(
                "averageDepositBondRatio",
                peerInvestment.averageDepositBondRatio(),
                "averageDomesticStockRatio",
                peerInvestment.averageDomesticStockRatio(),
                "averageForeignStockRatio",
                peerInvestment.averageForeignStockRatio(),
                "averageAlternativeRatio",
                peerInvestment.averageAlternativeRatio()
        );

        Map<String, Object> peerInvestmentData = Map.of(
                "peerCount", peerInvestment.peerCount(),
                "averageAmounts", peerAssetAmounts,
                "averageAllocationRatios", peerAssetRatios
        );

        /*
         * 양수: 사용자가 피어 그룹보다 비중이 높음
         * 음수: 사용자가 피어 그룹보다 비중이 낮음
         */
        Map<String, Object> allocationDifferences = Map.of(
                "depositBondPercentagePoints",
                calculateRatioDifference(
                        userAllocation.depositBondRatio(),
                        peerInvestment.averageDepositBondRatio()
                ),
                "domesticStockPercentagePoints",
                calculateRatioDifference(
                        userAllocation.domesticStockRatio(),
                        peerInvestment.averageDomesticStockRatio()
                ),
                "foreignStockPercentagePoints",
                calculateRatioDifference(
                        userAllocation.foreignStockRatio(),
                        peerInvestment.averageForeignStockRatio()
                ),
                "alternativePercentagePoints",
                calculateRatioDifference(
                        userAllocation.alternativeRatio(),
                        peerInvestment.averageAlternativeRatio()
                )
        );

        Map<String, Object> input = Map.of(
                "user", Map.of(
                        "financialProfile", userProfile,
                        "financialAsset", userAsset
                ),
                "peerGroup", Map.of(
                        "profileBenchmark", peerProfile,
                        "investmentBenchmark", peerInvestmentData
                ),
                "comparison", Map.of(
                        "allocationDifferencePercentagePoints",
                        allocationDifferences
                )
        );

        try {
            return objectMapper.writeValueAsString(input);
        } catch (JacksonException exception) {
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

    private double calculateRatioDifference(
            double userRatio,
            double peerAverageRatio
    ) {
        double difference = userRatio - peerAverageRatio;

        return Math.round(difference * 100.0) / 100.0;
    }
}
