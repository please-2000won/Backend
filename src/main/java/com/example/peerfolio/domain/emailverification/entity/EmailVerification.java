package com.example.peerfolio.domain.emailverification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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
public class EmailVerification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "email_verification_id")
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 6)
	private String code;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private Boolean verified;

	public static EmailVerification create(
			String email,
			String code,
			LocalDateTime expiresAt
	) {
		return EmailVerification.builder()
				.email(email)
				.code(code)
				.expiresAt(expiresAt)
				.verified(false)
				.build();
	}

	public void updateCode(
			String code,
			LocalDateTime expiresAt
	) {
		this.code = code;
		this.expiresAt = expiresAt;
		this.verified = false;
	}

	public boolean isExpired(LocalDateTime now) {
		return expiresAt.isBefore(now);
	}

	public boolean matches(String code) {
		return this.code.equals(code);
	}

	public void verify() {
		this.verified = true;
	}
}
