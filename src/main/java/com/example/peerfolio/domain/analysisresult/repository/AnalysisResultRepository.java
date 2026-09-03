package com.example.peerfolio.domain.analysisresult.repository;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findByUserId(Long userId);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from AnalysisResult ar
            where ar.user.id = :userId
            """)
    int deleteAllByUserId(@Param("userId") Long userId);

    Optional<AnalysisResult> findByUserIdAndInputHash(
            Long userId,
            String inputHash
    );
}
