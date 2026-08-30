package com.example.peerfolio.domain.peermatch.entity;

import com.example.peerfolio.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class PeerMatch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "peer_match_id")
	private Long id;

	@Column(nullable = false)
	private Double similarityScore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_user_id", nullable = false)
	private User targetUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "peer_user_id", nullable = false)
	private User peerUser;

	public static PeerMatch create(
			Double similarityScore,
			User targetUser,
			User peerUser
	) {
		return PeerMatch.builder()
				.similarityScore(similarityScore)
				.targetUser(targetUser)
				.peerUser(peerUser)
				.build();
	}
}
