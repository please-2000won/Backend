package com.example.peerfolio.domain.peermatch.service;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.peermatch.dto.PeerCardResponse;
import com.example.peerfolio.domain.peermatch.dto.PeerComparisonResponse;
import com.example.peerfolio.domain.peermatch.dto.PeerFinancialSummary;
import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import com.example.peerfolio.domain.peermatch.repository.PeerMatchRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeerQueryService {

    private static final int MIN_PEER_CARD_COUNT = 1;
    private static final int MAX_PEER_CARD_COUNT = 3;

    private final PeerMatchRepository peerMatchRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final FinancialAssetRepository financialAssetRepository;

    @Transactional(readOnly = true)
    public List<PeerCardResponse> getRandomPeers(
            Long userId,
            int size
    ) {
        validatePeerCardSize(size);

        List<PeerMatch> peerMatches =
                peerMatchRepository.findAllByTargetUserId(userId);

        if (peerMatches.isEmpty()) {
            throw new ProjectException(
                    GeneralErrorCode.NOT_FOUND
            );
        }

        // Repository 조회 결과를 직접 변경하지 않도록 복사
        List<PeerMatch> shuffledPeers =
                new ArrayList<>(peerMatches);

        // 요청마다 피어 순서를 무작위로 섞음
        Collections.shuffle(shuffledPeers);

        Map<Long, PeerMatch> uniquePeerMatches =
                new LinkedHashMap<>();

        for (PeerMatch peerMatch : shuffledPeers) {
            uniquePeerMatches.putIfAbsent(
                    peerMatch.getPeerUser().getId(),
                    peerMatch
            );
        }

        return uniquePeerMatches.values()
                .stream()
                .limit(size)
                .map(PeerCardResponse::from)
                .toList();
    }

    private void validatePeerCardSize(int size) {
        if (size < MIN_PEER_CARD_COUNT
                || size > MAX_PEER_CARD_COUNT) {
            throw new ProjectException(
                    GeneralErrorCode.BAD_REQUEST
            );
        }
    }

    @Transactional(readOnly = true)
    public PeerComparisonResponse getPeerComparison(
            User targetUser,
            Long peerUserId
    ) {
        // 선택된 사용자 현재 사용자의 피어인지 검증
        PeerMatch peerMatch = peerMatchRepository
                .findByTargetUserIdAndPeerUserId(
                        targetUser.getId(),
                        peerUserId
                )
                .orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );

        FinancialProfile myProfile = findFinancialProfile(targetUser.getId());
        FinancialAsset myAsset = findFinancialAsset(targetUser.getId());

        FinancialProfile peerProfile = findFinancialProfile(peerUserId);
        FinancialAsset peerAsset = findFinancialAsset(peerUserId);

        PeerFinancialSummary me =
                PeerFinancialSummary.of(
                        targetUser.getNickname(),
                        myProfile,
                        myAsset
                );

        PeerFinancialSummary peer =
                PeerFinancialSummary.of(
                        peerMatch.getPeerUser().getNickname(),
                        peerProfile,
                        peerAsset
                );

        return new PeerComparisonResponse(
                me, peer
        );
    }

    private FinancialProfile findFinancialProfile(Long userId) {
        return financialProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );
    }

    private FinancialAsset findFinancialAsset(Long userId) {
        return financialAssetRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );
    }
}
