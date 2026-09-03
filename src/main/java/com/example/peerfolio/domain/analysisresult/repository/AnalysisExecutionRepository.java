package com.example.peerfolio.domain.analysisresult.repository;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisExecutionRepository
        extends JpaRepository<AnalysisExecution, Long> {

    boolean existsByUserIdAndInputHash(
            Long userId,
            String inputHash
    );

    void deleteByUserIdAndInputHash(
            Long userId,
            String inputHash
    );
}
