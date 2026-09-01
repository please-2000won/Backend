package com.example.peerfolio.domain.peermatch.dto;

import com.example.peerfolio.domain.peermatch.entity.PeerMatch;

public record PeerCardResponse(
        Long peerUserId,
        String nickname,
        Double similarityScore
) {

    public static PeerCardResponse from(PeerMatch peerMatch) {
        return new PeerCardResponse(
                peerMatch.getPeerUser().getId(),
                peerMatch.getPeerUser().getNickname(),
                peerMatch.getSimilarityScore()
        );
    }
}
