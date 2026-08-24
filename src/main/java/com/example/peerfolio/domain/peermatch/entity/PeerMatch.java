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

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
}
