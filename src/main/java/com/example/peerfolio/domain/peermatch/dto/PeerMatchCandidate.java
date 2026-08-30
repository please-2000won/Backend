package com.example.peerfolio.domain.peermatch.dto;

public record PeerMatchCandidate (
        Long peerUserId,
        Double similarityScore
) {
}
