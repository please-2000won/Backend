package com.example.peerfolio.domain.peermatch.repository;

import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeerMatchRepository extends JpaRepository<PeerMatch, Long> {
}
