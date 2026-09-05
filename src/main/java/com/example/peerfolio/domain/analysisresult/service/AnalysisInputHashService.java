package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisInputHashService {

    private final FinancialProfileRepository financialProfileRepository;
    private final FinancialAssetRepository financialAssetRepository;

    private static final String ANALYSIS_POLICY_VERSION =
            "server-risk-score-v1";

    @Transactional(readOnly = true)
    public String generate(Long userId) {
        PeerProfileData profile = financialProfileRepository
                .findAllPeerProfileDataByUserIds(List.of(userId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));

        PeerAssetData asset = financialAssetRepository
                .findAllPeerAssetDataByUserIds(List.of(userId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));

        return generate(profile, asset);
    }

    public String generate(PeerProfileData profile, PeerAssetData asset) {
        // 필드 순서를 고정해 동일한 금융정보는 항상 동일한 해시를 생성
        String source = String.join("|",
                ANALYSIS_POLICY_VERSION,
                profile.age().toString(),
                profile.monthlyIncome().toString(),
                profile.fixedExpense().toString(),
                profile.savingsGoal().toString(),
                profile.totalAssetAmount().toString(),
                profile.totalDebtAmount().toString(),
                asset.depositBondAmount().toString(),
                asset.domesticStockAmount().toString(),
                asset.foreignStockAmount().toString(),
                asset.alternativeAmount().toString()
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(source.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new ProjectException(GeneralErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
