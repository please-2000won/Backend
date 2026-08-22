package com.example.peerfolio.domain.analysisresult.repository;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
}
