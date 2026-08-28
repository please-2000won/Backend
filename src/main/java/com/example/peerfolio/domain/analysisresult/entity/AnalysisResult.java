package com.example.peerfolio.domain.analysisresult.entity;

import com.example.peerfolio.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Table(name = "analysis_result")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalysisResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "analysis_result_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Integer peerCount;

	@Column(nullable = false, columnDefinition = "json")
	private String benchmarkResult;

	@Column(nullable = false, columnDefinition = "json")
	private String riskResult;

	@Column(nullable = false)
	private Integer totalRiskScore;

	@Column(nullable = false, columnDefinition = "text")
	private String analysisComment;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static AnalysisResult create(
			User user,
			Integer peerCount,
			String benchmarkResult,
			String riskResult,
			Integer totalRiskScore,
			String analysisComment
	) {
		return AnalysisResult.builder()
				.user(user)
				.peerCount(peerCount)
				.benchmarkResult(benchmarkResult)
				.riskResult(riskResult)
				.totalRiskScore(totalRiskScore)
				.analysisComment(analysisComment)
				.createdAt(LocalDateTime.now())
				.build();
	}
}
