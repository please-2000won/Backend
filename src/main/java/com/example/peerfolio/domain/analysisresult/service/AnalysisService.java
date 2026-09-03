package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.ai.AiAnalysisClient;
import com.example.peerfolio.domain.analysisresult.code.AnalysisErrorCode;
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
import org.springframework.dao.DataIntegrityViolationException;
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
    private final AnalysisInputHashService analysisInputHashService;
    private final ObjectMapper objectMapper;
    private final AnalysisExecutionService analysisExecutionService;

    // 외부 OpenAI 응답 기다리는 동안 DB 트랜잭션 유지하지 않음
    public AnalysisResponse createAnalysis(User user) {
        AnalysisPreparation preparation =
                analysisPreparationService.prepareAnalysis(
                        user.getId()
                );

        // 해시와 AI 분석이 반드시 같은 금융정보 스냅샷을 사용하도록 준비 결과에서 생성
        String currentInputHash = analysisInputHashService.generate(
                preparation.targetProfile(),
                preparation.targetAsset()
        );

        // 사용자 금융정보가 이전 분석 시점과 같으면 OpenAI를 다시 호출하지 않음
        AnalysisResult existingResult = analysisResultRepository
                .findByUserIdAndInputHash(
                        user.getId(),
                        currentInputHash
                )
                .orElse(null);

        if (existingResult != null) {
            return toResponse(existingResult, false);
        }

        // 실행 레코드 등록 시도, 동시 요청이 먼저 등록했다면 유니크 제약 위반 발생
        try {
            analysisExecutionService.claim(
                    user.getId(),
                    currentInputHash
            );
        } catch (DataIntegrityViolationException e) {
            throw new ProjectException(
                    AnalysisErrorCode.ANALYSIS_IN_PROGRESS
            );
        }

        try {
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
                            benchmarkResultJson,
                            currentInputHash
                    );

            return toResponse(analysisResult, false);
        } finally {
            // 성공 여부 관계 없이 실행 상태 제거
            analysisExecutionService.release(
                    user.getId(),
                    currentInputHash
            );
        }

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

        String currentInputHash = analysisInputHashService.generate(user.getId());
        boolean canReanalyze = !currentInputHash.equals(analysisResult.getInputHash());

        return toResponse(analysisResult, canReanalyze);
    }

    private AnalysisResponse toResponse(
            AnalysisResult analysisResult,
            boolean canReanalyze
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
                    analysisResult.getCreatedAt(),
                    canReanalyze
            );

        } catch (JacksonException exception) {
            throw new ProjectException(
                    GeneralErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }
}
