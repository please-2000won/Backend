package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisPreparation;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
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

@Service
@RequiredArgsConstructor
public class AnalysisResultWriter {

    private final UserRepository userRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PeerMatchRepository peerMatchRepository;

    @Transactional
    public AnalysisResult replaceAnalysisResult(
            Long userId,
            AnalysisPreparation preparation,
            AiAnalysisResult aiAnalysisResult,
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

        AnalysisResult analysisResult =
                AnalysisResult.create(
                        targetUser,
                        preparation.peerCandidates().size(),
                        benchmarkResultJson,
                        aiAnalysisResult.riskResult(),
                        aiAnalysisResult.totalRiskScore(),
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
}
