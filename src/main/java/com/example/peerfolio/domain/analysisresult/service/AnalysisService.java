package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.ai.AiAnalysisClient;
import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisPreparation;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisPreparationService analysisPreparationService;
    private final AiAnalysisClient aiAnalysisClient;
    private final AnalysisResultWriter analysisResultWriter;
    private final ObjectMapper objectMapper;

    // 외부 OpenAI 응답 기다리는 동안 DB 트랜잭션 유지하지 않음
    public AnalysisResult createAnalysis(User user) {
        // DB 변경하지 않고 피어 후보와 평균 준비
        AnalysisPreparation preparation =
                analysisPreparationService.prepareAnalysis(
                        user.getId()
                );

        // 직렬화 실패 시 요청 호출 전 분석 중단
        String benchmarkResultJson =
                serializeBenchmarkResult(
                        preparation.benchmarkResult()
                );

        // 외부 OpenAI API 호출
        AiAnalysisResult aiAnalysisResult =
                aiAnalysisClient.analyzePeerBenchmark(
                        preparation.targetProfile(),
                        preparation.targetAsset(),
                        preparation.benchmarkResult()
                );

        // 준비와 AI 분석 모두 성공한 경우에만 기존 피어 매칭과 분석 결과 교체
        return analysisResultWriter.replaceAnalysisResult(
                user.getId(),
                preparation,
                aiAnalysisResult,
                benchmarkResultJson
        );
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
}
