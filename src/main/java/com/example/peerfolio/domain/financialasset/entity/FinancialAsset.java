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
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column
	private LocalDateTime updatedAt;

	public static FinancialAsset create(
			User user,
			Long depositBondAmount,
			Long domesticStockAmount,
			Long foreignStockAmount,
			Long alternativeAmount
	) {
		return FinancialAsset.builder()
				.user(user)
				.depositBondAmount(depositBondAmount)
				.domesticStockAmount(domesticStockAmount)
				.foreignStockAmount(foreignStockAmount)
				.alternativeAmount(alternativeAmount)
				.updatedAt(currentUpdatedAt())
				.build();
	}

	public void update(
			Long depositBondAmount,
			Long domesticStockAmount,
			Long foreignStockAmount,
			Long alternativeAmount
	) {
		this.depositBondAmount = depositBondAmount;
		this.domesticStockAmount = domesticStockAmount;
		this.foreignStockAmount = foreignStockAmount;
		this.alternativeAmount = alternativeAmount;
		this.updatedAt = nextUpdatedAt(updatedAt);
	}

	@PrePersist
	private void prePersist() {
		if (updatedAt == null) {
			updatedAt = currentUpdatedAt();
		}
	}

	private static LocalDateTime currentUpdatedAt() {
		return LocalDateTime.now()
				.truncatedTo(ChronoUnit.MICROS);
	}

	private static LocalDateTime nextUpdatedAt(
			LocalDateTime previousUpdatedAt
	) {
		LocalDateTime now = currentUpdatedAt();

		if (previousUpdatedAt != null
				&& !now.isAfter(previousUpdatedAt)) {
			return previousUpdatedAt.plus(
					1,
					ChronoUnit.MICROS
			);
		}

		return now;
	}
}
