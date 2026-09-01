package com.example.peerfolio.domain.peermatch.service;

import com.example.peerfolio.domain.peermatch.dto.PeerCardResponse;
import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import com.example.peerfolio.domain.peermatch.repository.PeerMatchRepository;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeerQueryService {

    private static final int PEER_CARD_COUNT = 3;

    private final PeerMatchRepository peerMatchRepository;

    @Transactional(readOnly = true)
    public List<PeerCardResponse> getRandomPeers(Long userId) {
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

        return shuffledPeers.stream()
                .limit(PEER_CARD_COUNT)
                .map(PeerCardResponse::from)
                .toList();
    }
}
