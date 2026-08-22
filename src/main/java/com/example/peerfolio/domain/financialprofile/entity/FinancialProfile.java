package com.example.peerfolio.domain.financialprofile.entity;

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
public class FinancialProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "financial_profile_id")
	private Long id;

	@Column(nullable = false)
	private Integer age;

	@Column(nullable = false)
	private Long monthlyIncome;

	@Column(nullable = false)
	private Long fixedExpense;

	@Column(nullable = false)
	private Long savingsGoal;

	@Column(nullable = false)
	private Long totalAssetAmount;

	@Column(nullable = false)
	private Long totalDebtAmount;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
