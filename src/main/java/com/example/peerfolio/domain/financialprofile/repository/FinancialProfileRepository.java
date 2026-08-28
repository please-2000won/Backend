package com.example.peerfolio.domain.financialprofile.repository;

import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;

import java.util.List;
import java.util.Optional;

import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FinancialProfileRepository extends JpaRepository<FinancialProfile, Long> {

	Optional<FinancialProfile> findByUserId(Long userId);

	@Query("""
			select new com.example.peerfolio.domain.peermatch.dto.PeerProfileData(
				fp.user.id,
				fp.age,
				fp.monthlyIncome,
				fp.fixedExpense,
				fp.savingsGoal,
				fp.totalAssetAmount,
				fp.totalDebtAmount
			)
			from FinancialProfile fp
			""")
	List<PeerProfileData> findAllPeerProfileData();
}
