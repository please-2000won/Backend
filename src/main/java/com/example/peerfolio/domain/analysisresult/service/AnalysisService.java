package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.ai.AiAnalysisClient;
import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisPreparation;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisPreparationService analysisPreparationService;
    private final AiAnalysisClient aiAnalysisClient;
    private final AnalysisResultWriter analysisResultWriter;
    private final AnalysisResultRepository analysisResultRepository;
    private final ObjectMapper objectMapper;

    // 외부 OpenAI 응답 기다리는 동안 DB 트랜잭션 유지하지 않음
    public AnalysisResponse createAnalysis(User user) {
        AnalysisPreparation preparation =
                analysisPreparationService.prepareAnalysis(
                        user.getId()
                );

        String benchmarkResultJson =
                serializeBenchmarkResult(
                        preparation.benchmarkResult()
                );

        AiAnalysisResult aiAnalysisResult =
                aiAnalysisClient.analyzePeerBenchmark(
                        preparation.targetProfile(),
                        preparation.targetAsset(),
                        preparation.benchmarkResult()
                );

        AnalysisResult analysisResult =
                analysisResultWriter.replaceAnalysisResult(
                        user.getId(),
                        preparation,
                        aiAnalysisResult,
                        benchmarkResultJson
                );

        return toResponse(analysisResult);
    }

    private String serializeBenchmarkResult(BenchmarkResult benchmarkResult) {
        try {
            return objectMapper.writeValueAsString(benchmarkResult);
        } catch (JacksonException e) {
            throw new ProjectException(
                    GeneralErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getLatestAnalysis(User user) {
        AnalysisResult analysisResult =
                analysisResultRepository.findByUserId(
                        user.getId()
                ).orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );

        return toResponse(analysisResult);
    }

    private AnalysisResponse toResponse(
            AnalysisResult analysisResult
    ) {
        try {
            BenchmarkResult benchmarkResult =
                    objectMapper.readValue(
                            analysisResult.getBenchmarkResult(),
                            BenchmarkResult.class
                    );

            AnalysisResponse.RiskResult riskResult =
                    objectMapper.readValue(
                            analysisResult.getRiskResult(),
                            AnalysisResponse.RiskResult.class
                    );

            return new AnalysisResponse(
                    analysisResult.getId(),
                    analysisResult.getPeerCount(),
                    benchmarkResult,
                    riskResult,
                    analysisResult.getTotalRiskScore(),
                    analysisResult.getAnalysisComment(),
                    analysisResult.getCreatedAt()
            );

        } catch (JacksonException exception) {
            throw new ProjectException(
                    GeneralErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }
}
