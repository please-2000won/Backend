package com.example.peerfolio.global.security;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

	private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

	public void blacklist(
			String token,
			Date expiresAt
	) {
		blacklistedTokens.put(token, expiresAt.getTime());
	}

	public boolean isBlacklisted(String token) {
		Long expiresAt = blacklistedTokens.get(token);

		if (expiresAt == null) {
			return false;
		}

		if (expiresAt < System.currentTimeMillis()) {
			blacklistedTokens.remove(token);
			return false;
		}

		return true;
	}
}
