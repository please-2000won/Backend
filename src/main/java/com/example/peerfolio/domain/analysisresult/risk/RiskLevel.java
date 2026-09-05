package com.example.peerfolio.domain.analysisresult.risk;

public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH;

    public static RiskLevel from(int score) {
        if (score <= 33) return LOW;
        if (score <= 66) return MEDIUM;
        return HIGH;
    }
}
