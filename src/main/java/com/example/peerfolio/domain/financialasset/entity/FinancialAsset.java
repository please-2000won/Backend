package com.example.peerfolio.domain.financialasset.entity;

import com.example.peerfolio.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialAsset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "financial_asset_id")
	private Long id;

	@Column(nullable = false)
	private Long depositBondAmount;

	@Column(nullable = false)
	private Long domesticStockAmount;

	@Column(nullable = false)
	private Long foreignStockAmount;

	@Column(nullable = false)
	private Long alternativeAmount;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
