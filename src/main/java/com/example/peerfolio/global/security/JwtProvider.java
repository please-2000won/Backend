package com.example.peerfolio.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

	private static final int MIN_SECRET_LENGTH_BYTES = 32;

	private final SecretKey secretKey;
	private final long expiration;

	public JwtProvider(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration}") long expiration
	) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException(
					"JWT_SECRET 환경변수를 설정해야 합니다."
			);
		}

		byte[] keyBytes;

		try {
			keyBytes = Decoders.BASE64.decode(secret);
		} catch (Exception exception) {
			throw new IllegalStateException(
					"JWT_SECRET은 Base64 형식이어야 합니다.",
					exception
			);
		}

		if (keyBytes.length < MIN_SECRET_LENGTH_BYTES) {
			throw new IllegalStateException(
					"JWT_SECRET은 디코딩 기준 최소 32바이트여야 합니다."
			);
		}

		if (expiration <= 0) {
			throw new IllegalStateException(
					"JWT_EXPIRATION은 0보다 커야 합니다."
			);
		}

		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.expiration = expiration;
	}

	public String createAccessToken(Long userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(String.valueOf(userId))
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(secretKey)
				.compact();
	}

	public Long getUserId(String token) {
		return Long.valueOf(parseClaims(token).getSubject());
	}

	public Date getExpiration(String token) {
		return parseClaims(token).getExpiration();
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
