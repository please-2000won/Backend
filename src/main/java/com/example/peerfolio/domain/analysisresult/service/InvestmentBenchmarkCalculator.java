package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvestmentBenchmarkCalculator {

    public InvestmentBenchmark calculateInvestmentBenchmark(
            List<PeerAssetData> peerAssetDataList
    ) {
        if (peerAssetDataList.isEmpty()) {
            throw new IllegalArgumentException(
                    "피어 자산 정보가 존재하지 않습니다."
            );
        }

        int peerCount = peerAssetDataList.size();

        // 피어들의 자산 유형별 평균 금액 계산
        long averageDepositBondAmount = calculateAverageAmount(
                peerAssetDataList.stream()
                        .mapToLong(PeerAssetData::depositBondAmount)
                        .sum(),
                peerCount
        );

        long averageDomesticStockAmount = calculateAverageAmount(
                peerAssetDataList.stream()
                        .mapToLong(PeerAssetData::domesticStockAmount)
                        .sum(),
                peerCount
        );

        long averageForeignStockAmount = calculateAverageAmount(
                peerAssetDataList.stream()
                        .mapToLong(PeerAssetData::foreignStockAmount)
                        .sum(),
                peerCount
        );

        long averageAlternativeAmount = calculateAverageAmount(
                peerAssetDataList.stream()
                        .mapToLong(PeerAssetData::alternativeAmount)
                        .sum(),
                peerCount
        );

        // 사용자별 투자 비율 계산 후 그 비율의 평균 냄
        // 단순히 피어 전체 금액 합산해서 비율 구할 시 자산 많은 사용자가 평균 크게 좌우 가능
        double averageDepositBondRatio = peerAssetDataList.stream()
                .mapToDouble(data -> calculateRatio(
                        data.depositBondAmount(),
                        calculateTotalInvestment(data)
                ))
                .average()
                .orElse(0.0);

        double averageDomesticStockRatio = peerAssetDataList.stream()
                .mapToDouble(data -> calculateRatio(
                        data.domesticStockAmount(),
                        calculateTotalInvestment(data)
                ))
                .average()
                .orElse(0.0);

        double averageForeignStockRatio = peerAssetDataList.stream()
                .mapToDouble(data -> calculateRatio(
                        data.foreignStockAmount(),
                        calculateTotalInvestment(data)
                ))
                .average()
                .orElse(0.0);

        double averageAlternativeRatio = peerAssetDataList.stream()
                .mapToDouble(data -> calculateRatio(
                        data.alternativeAmount(),
                        calculateTotalInvestment(data)
                ))
                .average()
                .orElse(0.0);

        return new InvestmentBenchmark(
                peerCount,
                averageDepositBondAmount,
                averageDomesticStockAmount,
                averageForeignStockAmount,
                averageAlternativeAmount,
                roundRatio(averageDepositBondRatio),
                roundRatio(averageDomesticStockRatio),
                roundRatio(averageForeignStockRatio),
                roundRatio(averageAlternativeRatio)
        );
    }

    private long calculateAverageAmount(
            long totalAmount,
            int peerCount
    ) {
        return Math.round((double) totalAmount / peerCount);
    }

    private double calculateRatio(long amount, long totalAmount) {
        if (totalAmount <= 0) {
            return 0.0;
        }

        return (double) amount / totalAmount * 100.0;
    }

    private long calculateTotalInvestment(PeerAssetData data) {
        return data.depositBondAmount()
                + data.domesticStockAmount()
                + data.foreignStockAmount()
                + data.alternativeAmount();
    }

    // 소수점 둘째 자리까지 반올림
    private double roundRatio(double ratio) {
        return Math.round(ratio * 100.0) / 100.0;
    }
}
