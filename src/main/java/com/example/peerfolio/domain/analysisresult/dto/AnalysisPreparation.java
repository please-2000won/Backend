package com.example.peerfolio.domain.analysisresult.dto;

import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerMatchCandidate;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import java.util.List;

public record AnalysisPreparation(
        PeerProfileData targetProfile,
        PeerAssetData targetAsset,
        List<PeerMatchCandidate> peerCandidates,
        BenchmarkResult benchmarkResult
) {
}
