package com.example.peerfolio.domain.financialasset.repository;

import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;

import java.util.List;
import java.util.Optional;

import com.example.peerfolio.domain.peermatch.dto.PeerAssetData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAssetRepository extends JpaRepository<FinancialAsset, Long> {

	Optional<FinancialAsset> findByUserId(Long userId);

	@Query("""
			select new com.example.peerfolio.domain.peermatch.dto.PeerAssetData(
			fa.user.id,
			fa.depositBoindAmount,
			fa.domesticStockAmount,
			fa.foreignStockAmount,
			fa.alternativeAmount)
			from FinancialAsset fa 
			where fa.user.id in :userIds
			""")
	List<PeerAssetData> findAllPeerAssetDateByUserIds(@Param("userIds") List<Long> userIds);
}
