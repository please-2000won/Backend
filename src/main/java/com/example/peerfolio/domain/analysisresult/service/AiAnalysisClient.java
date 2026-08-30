package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.dto.AiAnalysisResult;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;

public interface AiAnalysisClient {

    AiAnalysisResult analyzePeerBenchmark(
            PeerProfileData targetProfile,
            PeerAssetData targetAsset,
            BenchmarkResult benchmarkResult
    );
}
