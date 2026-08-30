package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.analysisresult.dto.PeerProfileBenchmark;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import com.example.peerfolio.domain.peermatch.service.PeerMatchingService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeerBenchmarkService {

    private final PeerMatchingService peerMatchingService;
    private final FinancialProfileRepository financialProfileRepository;
    private final FinancialAssetRepository financialAssetRepository;
    private final PeerProfileBenchmarkCalculator peerProfileBenchmarkCalculator;
    private final InvestmentBenchmarkCalculator investmentBenchmarkCalculator;

    @Transactional
    public BenchmarkResult createPeerBenchmark(User targetUser) {

        // 현재 금융 프로필 기준으로 피어 그룹 재생성, 기존 PeerMatch는 서비스 내부에서 삭제
        List<PeerMatch> peerMatchList =
                peerMatchingService.replaceMatchingPeers(targetUser);

        // 선정된 피어 아이디만 추출
        List<Long> peerUserIds = peerMatchList.stream()
                .map(peerMatch -> peerMatch.getPeerUser().getId())
                .toList();

        // 선정된 피어들의 프로필과 투자자산 각각 조회
        List<PeerProfileData> peerProfileDataList =
                financialProfileRepository
                        .findAllPeerProfileDataByUserIds(peerUserIds);

        List<PeerAssetData> peerAssetDataList =
                financialAssetRepository
                        .findAllPeerAssetDataByUserIds(peerUserIds);

        // 매칭 피어 중 금융정보 누락 사용자 있을 시 분석 중단
        if (peerProfileDataList.size() != peerUserIds.size()
                || peerAssetDataList.size() != peerUserIds.size()) {
            throw new ProjectException(GeneralErrorCode.NOT_FOUND);
        }

        // 경제적 여건 평균과 투자자산 평균 각각 계산
        PeerProfileBenchmark profileBenchmark =
                peerProfileBenchmarkCalculator.calculatePeerProfileBenchmark(peerProfileDataList);

        InvestmentBenchmark investmentBenchmark =
                investmentBenchmarkCalculator.calculateInvestmentBenchmark(peerAssetDataList);

        // 두 계산 결과 묶음
        return new BenchmarkResult(profileBenchmark, investmentBenchmark);
    }
}
