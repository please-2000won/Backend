package com.example.peerfolio.domain.analysisresult.dto;

public record BenchmarkResult(
        PeerProfileBenchmark profile,
        InvestmentBenchmark investment
) {
}
