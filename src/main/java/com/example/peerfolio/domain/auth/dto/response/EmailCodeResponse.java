package com.example.peerfolio.domain.auth.dto.response;

import java.time.LocalDateTime;

public record EmailCodeResponse(
		String email,
		String verificationCode,
		LocalDateTime expiresAt
) {
}
