package com.example.peerfolio.domain.peermatch.service;

import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.peermatch.dto.PeerMatchCandidate;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import com.example.peerfolio.domain.peermatch.repository.PeerMatchRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeerMatchingService {

    // 유사도 60점 이상인 사용자 피어로 선정
    // 수정 가능
    private static final double MIN_SIMILARITY_SCORE = 60.0;

    // 피어 카드 랜덤 3개 필요하므로 최소 3명 선정할 수 있게 설정
    private static final int MIN_PEER_COUNT = 3;

    private final FinancialProfileRepository financialProfileRepository;
    private final PeerSimilarityCalculator peerSimilarityCalculator;

    private final PeerMatchRepository peerMatchRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PeerMatchCandidate> findMatchingPeers(Long targetUserId) {
        // 금융 프로필 입력한 모든 사용자 한번에 조회
        List<PeerProfileData> profileDataList =
                financialProfileRepository.findAllPeerProfileData();

        // 조회 결과에서 분석 대상 사용자 금융 프로필 찾음
        PeerProfileData targetProfile = profileDataList.stream()
                .filter(data -> data.userId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() ->
                        new ProjectException(GeneralErrorCode.BAD_REQUEST)
                );

        // 본인 제외한 모든 사용자의 유사도 계산
        List<PeerMatchCandidate> scoredCandidates = profileDataList.stream()
                .filter(data -> !data.userId().equals(targetUserId))
                .map(candidate -> new PeerMatchCandidate(
                        candidate.userId(),
                        peerSimilarityCalculator.calculateSimilarity(
                                targetProfile,
                                candidate
                        )
                ))
                .sorted(
                        Comparator.comparing(
                                PeerMatchCandidate::similarityScore
                        ).reversed()
                )
                .toList();

        // 자신 외에 금융 정보 입력한 사용자 없을 시
        if (scoredCandidates.isEmpty()) {
            throw new ProjectException(GeneralErrorCode.NOT_FOUND);
        }

        // 유사도 기준 통과한 사용자 수 계산
        long qualifiedPeerCount = scoredCandidates.stream()
                .filter(candidate ->
                        candidate.similarityScore()
                            >= MIN_SIMILARITY_SCORE
                )
                .count();

        // 기준 통과한 피어 모두 포함
        // 3명 미만일 시 상위 후보 최대 3명까지 포함
        int selectedPeerCount = Math.min(
                scoredCandidates.size(),
                Math.max(
                        MIN_PEER_COUNT,
                        Math.toIntExact(qualifiedPeerCount)
                )
        );

        return scoredCandidates.stream()
                .limit(selectedPeerCount)
                .toList();
    }

    // 매칭 교체
    @Transactional
    public List<PeerMatch> replaceMatchingPeers(User targetUser) {
        // 현재 금융 프로필을 기준으로 새로운 피어 후보 선정
        List<PeerMatchCandidate> candidates = findMatchingPeers(targetUser.getId());

        // 재분석 시 이전 피어 매칭 결과 삭제
        peerMatchRepository.deleteAllByTargetUserId(targetUser.getId());

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

        return peerMatchRepository.saveAll(peerMatches);
    }
}
