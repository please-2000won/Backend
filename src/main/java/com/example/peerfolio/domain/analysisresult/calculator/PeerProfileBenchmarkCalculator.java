package com.example.peerfolio.domain.analysisresult.calculator;

import com.example.peerfolio.domain.analysisresult.dto.PeerProfileBenchmark;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PeerProfileBenchmarkCalculator {

    public PeerProfileBenchmark calculatePeerProfileBenchmark(
            List<PeerProfileData> peerProfileDataList
    ) {
        if (peerProfileDataList.isEmpty()) {
            throw new IllegalArgumentException(
                    "피어 프로필 정보가 존재하지 않습니다."
            );
        }

        long averageMonthlyIncome = Math.round(
                peerProfileDataList.stream()
                        .mapToLong(PeerProfileData::monthlyIncome)
                        .average()
                        .orElse(0.0)
        );

        long averageTotalAssetAmount = Math.round(
                peerProfileDataList.stream()
                        .mapToLong(PeerProfileData::totalAssetAmount)
                        .average()
                        .orElse(0.0)
        );

        return new PeerProfileBenchmark(
                averageMonthlyIncome,
                averageTotalAssetAmount
        );
    }
}
