package com.example.peerfolio.domain.analysisresult.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.peerfolio.domain.analysisresult.ai.AiAnalysisClient;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisPreparation;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.analysisresult.dto.PeerProfileBenchmark;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import com.example.peerfolio.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AnalysisServiceTest {

    @Test
    void returnsExistingResultWithoutCallingOpenAiWhenInputIsUnchanged() throws Exception {
        AnalysisPreparationService preparationService = mock(AnalysisPreparationService.class);
        AiAnalysisClient aiAnalysisClient = mock(AiAnalysisClient.class);
        AnalysisResultWriter resultWriter = mock(AnalysisResultWriter.class);
        AnalysisResultRepository resultRepository = mock(AnalysisResultRepository.class);
        AnalysisInputHashService hashService = mock(AnalysisInputHashService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        AnalysisService analysisService = new AnalysisService(
                preparationService,
                aiAnalysisClient,
                resultWriter,
                resultRepository,
                hashService,
                objectMapper
        );

        User user = mock(User.class);
        AnalysisResult existingResult = mock(AnalysisResult.class);
        BenchmarkResult benchmarkResult = new BenchmarkResult(
                new PeerProfileBenchmark(2_500_000L, 10_000_000L),
                new InvestmentBenchmark(3, 1L, 2L, 3L, 4L, 10.0, 20.0, 30.0, 40.0)
        );
        AnalysisResponse.RiskResult riskResult =
                new AnalysisResponse.RiskResult("LOW", "위험도가 낮습니다.");
        PeerProfileData targetProfile = new PeerProfileData(
                1L, 25, 2_500_000L, 800_000L, 500_000L, 10_000_000L, 2_000_000L
        );
        PeerAssetData targetAsset = new PeerAssetData(
                1L, 1_000_000L, 2_000_000L, 3_000_000L, 500_000L
        );
        AnalysisPreparation preparation = new AnalysisPreparation(
                targetProfile,
                targetAsset,
                java.util.List.of(),
                benchmarkResult
        );

        when(user.getId()).thenReturn(1L);
        when(preparationService.prepareAnalysis(1L)).thenReturn(preparation);
        when(hashService.generate(targetProfile, targetAsset)).thenReturn("same-hash");
        when(resultRepository.findByUserId(1L)).thenReturn(Optional.of(existingResult));
        when(existingResult.getInputHash()).thenReturn("same-hash");
        when(existingResult.getId()).thenReturn(10L);
        when(existingResult.getPeerCount()).thenReturn(3);
        when(existingResult.getBenchmarkResult())
                .thenReturn(objectMapper.writeValueAsString(benchmarkResult));
        when(existingResult.getRiskResult())
                .thenReturn(objectMapper.writeValueAsString(riskResult));
        when(existingResult.getTotalRiskScore()).thenReturn(20);
        when(existingResult.getAnalysisComment()).thenReturn("기존 분석입니다.");
        when(existingResult.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 9, 3, 10, 0));

        AnalysisResponse response = analysisService.createAnalysis(user);

        assertEquals(10L, response.analysisResultId());
        assertFalse(response.canReanalyze());
        verify(aiAnalysisClient, never()).analyzePeerBenchmark(null, null, null);
        verify(resultWriter, never()).replaceAnalysisResult(null, null, null, null, null);
    }
}
