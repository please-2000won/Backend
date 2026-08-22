package com.example.peerfolio.domain.analysisresult.entity;

import com.example.peerfolio.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "analysis_result_id")
	private Long id;

	@Column(nullable = false)
	private Integer peerCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(columnDefinition = "json")
	private String benchmarkResult;

	@Column(columnDefinition = "json")
	private String riskResult;

	@Column(nullable = false)
	private Integer totalRiskScore;

	@Column(nullable = false)
	private LocalDateTime createdAt;
}
