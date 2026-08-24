package com.example.peerfolio.domain.financialasset.repository;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialAssetRepository extends JpaRepository<FinancialAsset, Long> {
}
