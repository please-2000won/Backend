package com.example.peerfolio.domain.peermatch.repository;

import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PeerMatchRepository extends JpaRepository<PeerMatch, Long> {

    @EntityGraph(attributePaths = "peerUser")
    List<PeerMatch> findAllByTargetUserId(Long targetUserId);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from PeerMatch pm
            where pm.targetUser.id = :targetUserId
            """)
    int deleteAllByTargetUserId(@Param("targetUserId") Long targetUserId);
}
