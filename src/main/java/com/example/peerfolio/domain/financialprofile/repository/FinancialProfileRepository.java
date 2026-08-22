package com.example.peerfolio.domain.financialprofile.repository;

import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialProfileRepository extends JpaRepository<FinancialProfile, Long> {
}
