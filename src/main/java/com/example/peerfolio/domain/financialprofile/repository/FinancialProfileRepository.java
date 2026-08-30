package com.example.peerfolio.domain.financialprofile.repository;

import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;

import java.util.List;
import java.util.Optional;

import com.example.peerfolio.domain.peermatch.dto.PeerProfileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
      		where exists (
         		select 1
         		from FinancialAsset fa
         		where fa.user.id = fp.user.id
      		)
      		""")
	List<PeerProfileData> findAllPeerProfileData();

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
			where fp.user.id in :userIds
			""")
	List<PeerProfileData> findAllPeerProfileDataByUserIds(
			@Param("userIds") List<Long> userIds
	);
}
