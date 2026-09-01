package com.example.peerfolio.domain.peermatch.dto;

public record PeerComparisonResponse(
        PeerFinancialSummary me,
        PeerFinancialSummary peer
) {
}
