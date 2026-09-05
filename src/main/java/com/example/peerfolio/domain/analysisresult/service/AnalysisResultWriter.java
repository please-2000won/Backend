package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisPreparation;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.analysisresult.risk.RiskScoreResult;
import com.example.peerfolio.domain.peermatch.dto.PeerMatchCandidate;
import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import com.example.peerfolio.domain.peermatch.repository.PeerMatchRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AnalysisResultWriter {

    private final UserRepository userRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PeerMatchRepository peerMatchRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisResult replaceAnalysisResult(
            Long userId,
            AnalysisPreparation preparation,
            AiAnalysisResult aiAnalysisResult,
            RiskScoreResult riskScoreResult,
            String benchmarkResultJson,
            String inputHash
    ) {
        // 같은 사용자의 저장 작업이 동시에 진행되지 않도록 잠금을 걸어둠
        User targetUser = userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );

        // AI 분석이 성공한 이후 기존 결과를 교체
        analysisResultRepository.deleteAllByUserId(userId);

        replacePeerMatches(
                targetUser,
                preparation.peerCandidates()
        );

        String riskResultJson = serializeRiskResult(
                riskScoreResult,
                aiAnalysisResult
        );

        AnalysisResult analysisResult =
                AnalysisResult.create(
                        targetUser,
                        preparation.peerCandidates().size(),
                        benchmarkResultJson,
                        riskResultJson,
                        riskScoreResult.totalRiskScore(),
                        aiAnalysisResult.analysisComment(),
                        inputHash
                );

        return analysisResultRepository.save(
                analysisResult
        );
    }

    private void replacePeerMatches(
            User targetUser,
            List<PeerMatchCandidate> candidates
    ) {
        peerMatchRepository.deleteAllByTargetUserId(
                targetUser.getId()
        );

        List<PeerMatch> peerMatches = candidates.stream()
                .map(candidate -> {
                    User peerUser = userRepository.getReferenceById(
                            candidate.peerUserId()
                    );

                    return PeerMatch.create(
                            candidate.similarityScore(),
                            targetUser,
                            peerUser
                    );
                })
                .toList();

        peerMatchRepository.saveAll(peerMatches);
    }

    private String serializeRiskResult(
            RiskScoreResult riskScoreResult,
            AiAnalysisResult aiAnalysisResult
    ) {
        AnalysisResponse.RiskResult riskResult =
                new AnalysisResponse.RiskResult(
                        riskScoreResult.riskLevel().name(),
                        aiAnalysisResult.riskSummary()
                );

        try {
            return objectMapper.writeValueAsString(riskResult);
        } catch (JacksonException e) {
            throw new ProjectException(
                    GeneralErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }
}
