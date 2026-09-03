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
	@JoinColumn(name = "user_id", nullable = false, unique = true)
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

	// 분석에 사용한 사용자 금융정보의 해시값
	// 기존 운영 데이터와의 호환을 위해 nullable로 둠, 새 분석부터 저장
	@Column(length = 64)
	private String inputHash;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static AnalysisResult create(
			User user,
			Integer peerCount,
			String benchmarkResult,
			String riskResult,
			Integer totalRiskScore,
			String analysisComment,
			String inputHash
	) {
		return AnalysisResult.builder()
				.user(user)
				.peerCount(peerCount)
				.benchmarkResult(benchmarkResult)
				.riskResult(riskResult)
				.totalRiskScore(totalRiskScore)
				.analysisComment(analysisComment)
				.inputHash(inputHash)
				.createdAt(LocalDateTime.now())
				.build();
	}
}
