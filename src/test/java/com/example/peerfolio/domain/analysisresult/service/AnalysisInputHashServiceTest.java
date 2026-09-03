package com.example.peerfolio.domain.analysisresult.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import org.junit.jupiter.api.Test;

class AnalysisInputHashServiceTest {

    private final AnalysisInputHashService hashService =
            new AnalysisInputHashService(null, null);

    @Test
    void sameFinancialInformationCreatesSameHash() {
        PeerProfileData profile = profile(2_500_000L);
        PeerAssetData asset = asset(1_000_000L);

        assertEquals(
                hashService.generate(profile, asset),
                hashService.generate(profile, asset)
        );
    }

    @Test
    void changedFinancialInformationCreatesDifferentHash() {
        String originalHash = hashService.generate(
                profile(2_500_000L),
                asset(1_000_000L)
        );

        String changedHash = hashService.generate(
                profile(2_600_000L),
                asset(1_000_000L)
        );

        assertNotEquals(originalHash, changedHash);
    }

    private PeerProfileData profile(Long monthlyIncome) {
        return new PeerProfileData(
                1L,
                25,
                monthlyIncome,
                800_000L,
                500_000L,
                10_000_000L,
                2_000_000L
        );
    }

    private PeerAssetData asset(Long depositBondAmount) {
        return new PeerAssetData(
                1L,
                depositBondAmount,
                2_000_000L,
                3_000_000L,
                500_000L
        );
    }
}
