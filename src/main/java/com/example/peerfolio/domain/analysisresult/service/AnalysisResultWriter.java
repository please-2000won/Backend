package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisPreparation;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.peermatch.service.PeerMatchingService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisResultWriter {

    private final UserRepository userRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PeerMatchingService peerMatchingService;

    @Transactional
    public AnalysisResult replaceAnalysisResult(
            Long userId,
            AnalysisPreparation preparation,
            AiAnalysisResult aiAnalysisResult,
            String benchmarkResultJson
    ) {
        // 같은 사용자의 분석 요청이 동시에 실행될 경우 저장 단계가 겹치지 않도록 사용자 행에 쓰기 잠금을 걸어둠
        User targetUser = userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );

        // 이 시점은 벤치마크 계산과 OpenAI 분석이 모두 성공한 이후이므로 기존 결과를 교체해도 됨
        analysisResultRepository.deleteAllByUserId(userId);

        peerMatchingService.replaceMatchingPeers(
                targetUser,
                preparation.peerCandidates()
        );

        AnalysisResult analysisResult =
                AnalysisResult.create(
                        targetUser,
                        preparation.peerCandidates().size(),
                        benchmarkResultJson,
                        aiAnalysisResult.riskResult(),
                        aiAnalysisResult.totalRiskScore(),
                        aiAnalysisResult.analysisComment()
                );

        return analysisResultRepository.save(
                analysisResult
        );
    }
}
