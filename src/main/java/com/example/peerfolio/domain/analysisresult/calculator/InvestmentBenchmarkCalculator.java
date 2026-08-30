package com.example.peerfolio.domain.analysisresult.calculator;

import com.example.peerfolio.domain.analysisresult.dto.InvestmentAllocation;
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

        // 각 피어의 자산 구성 비율을 먼저 계산
        List<InvestmentAllocation> allocations =
                peerAssetDataList.stream()
                        .map(this::calculateAllocation)
                        .toList();

        // 각 사용자 비율의 평균을 피어 그룹 평균 비율로 사용
        double averageDepositBondRatio = allocations.stream()
                .mapToDouble(InvestmentAllocation::depositBondRatio)
                .average()
                .orElse(0.0);

        double averageDomesticStockRatio = allocations.stream()
                .mapToDouble(InvestmentAllocation::domesticStockRatio)
                .average()
                .orElse(0.0);

        double averageForeignStockRatio = allocations.stream()
                .mapToDouble(InvestmentAllocation::foreignStockRatio)
                .average()
                .orElse(0.0);

        double averageAlternativeRatio = allocations.stream()
                .mapToDouble(InvestmentAllocation::alternativeRatio)
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

    // 사용자 한명의 전체 투자자산에서 각 자산 유형이 차지하는 비율 계산
    public InvestmentAllocation calculateAllocation(
            PeerAssetData assetData
    ) {
        long totalInvestment = calculateTotalInvestment(assetData);

        return new InvestmentAllocation(
                calculateRatio(
                        assetData.depositBondAmount(),
                        totalInvestment
                ),
                calculateRatio(
                        assetData.domesticStockAmount(),
                        totalInvestment
                ),
                calculateRatio(
                        assetData.foreignStockAmount(),
                        totalInvestment
                ),
                calculateRatio(
                        assetData.alternativeAmount(),
                        totalInvestment
                )
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
