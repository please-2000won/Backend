package com.example.peerfolio.domain.analysisresult.repository;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

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

    @Modifying
    @Query("""
        delete from AnalysisExecution ae
        where ae.user.id = :userId
          and ae.inputHash = :inputHash
          and ae.expiresAt <= :now
        """)
    int deleteExpired(
            @Param("userId") Long userId,
            @Param("inputHash") String inputHash,
            @Param("now") LocalDateTime now
    );
}
