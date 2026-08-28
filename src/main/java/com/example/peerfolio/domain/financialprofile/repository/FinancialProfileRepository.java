package com.example.peerfolio.domain.financialprofile.repository;

import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;

import java.util.List;
import java.util.Optional;

import com.example.peerfolio.domain.peermatch.dto.PeerFinancialData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FinancialProfileRepository extends JpaRepository<FinancialProfile, Long> {

	Optional<FinancialProfile> findByUserId(Long userId);

	@Query("""
			select new com.example.peerfolio.domain.peermatch.dto.PeerFinancialData
				fp.user.id,
				fp.age,
				fp.monthlyIncome,
				fp.savingsGoal,
				fp.totalAssetAmount,
				fp.depositBondAmount,
				fa.depositBondAmount,
				fa.domesticStockAmount,
				fa.foreignStockAmount,
				fa.alternativeAmount
			)
			from FinancialProfile fp, FinancialAsset fa 
			where fa.user.id = fp.user.id
			""")
	List<PeerFinancialData> findAllPeerFinancialData();
}
