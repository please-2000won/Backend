package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.calculator.InvestmentBenchmarkCalculator;
import com.example.peerfolio.domain.analysisresult.calculator.PeerProfileBenchmarkCalculator;
import com.example.peerfolio.domain.analysisresult.dto.AnalysisPreparation;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.analysisresult.dto.PeerProfileBenchmark;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerMatchCandidate;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import com.example.peerfolio.domain.peermatch.service.PeerMatchingService;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisPreparationService {

    private final PeerMatchingService peerMatchingService;
    private final FinancialProfileRepository financialProfileRepository;
    private final FinancialAssetRepository financialAssetRepository;
    private final PeerProfileBenchmarkCalculator peerProfileBenchmarkCalculator;
    private final InvestmentBenchmarkCalculator investmentBenchmarkCalculator;

    @Transactional(readOnly = true)
    public AnalysisPreparation prepareAnalysis(Long targetUserId) {
        // 아직 기존 PeerMatch를 삭제하거나 새로운 값을 저장하지 않고 현재 금융정보를 기준으로 새로운 후보만 계산
        List<PeerMatchCandidate> peerCandidates =
                peerMatchingService.findMatchingPeers(targetUserId);

        List<Long> peerUserIds = peerCandidates.stream()
                .map(PeerMatchCandidate::peerUserId)
                .toList();

        PeerProfileData targetProfile =
                findTargetProfile(targetUserId);

        PeerAssetData targetAsset =
                findTargetAsset(targetUserId);

        List<PeerProfileData> peerProfileDataList =
                financialProfileRepository
                        .findAllPeerProfileDataByUserIds(peerUserIds);

        List<PeerAssetData> peerAssetDataList =
                financialAssetRepository
                        .findAllPeerAssetDataByUserIds(peerUserIds);

        // 선정된 피어 중 프로필이나 금융자산이 누락됐다면 잘못된 평균을 생성하지 않고 분석을 중단
        if (peerProfileDataList.size() != peerUserIds.size()
                || peerAssetDataList.size() != peerUserIds.size()) {
            throw new ProjectException(
                    GeneralErrorCode.NOT_FOUND
            );
        }

        PeerProfileBenchmark profileBenchmark =
                peerProfileBenchmarkCalculator
                        .calculatePeerProfileBenchmark(
                                peerProfileDataList
                        );

        InvestmentBenchmark investmentBenchmark =
                investmentBenchmarkCalculator
                        .calculateInvestmentBenchmark(
                                peerAssetDataList
                        );

        BenchmarkResult benchmarkResult =
                new BenchmarkResult(
                        profileBenchmark,
                        investmentBenchmark
                );

        return new AnalysisPreparation(
                targetProfile,
                targetAsset,
                peerCandidates,
                benchmarkResult
        );
    }

    private PeerProfileData findTargetProfile(Long targetUserId) {
        return financialProfileRepository
                .findAllPeerProfileDataByUserIds(
                        List.of(targetUserId)
                )
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );
    }

    private PeerAssetData findTargetAsset(Long targetUserId) {
        return financialAssetRepository
                .findAllPeerAssetDataByUserIds(
                        List.of(targetUserId)
                )
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new ProjectException(
                                GeneralErrorCode.NOT_FOUND
                        )
                );
    }
}
