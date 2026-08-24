package com.example.peerfolio.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "이메일 인증번호 발급 응답")
public record EmailCodeResponse(
		@Schema(description = "인증번호를 발급한 이메일", example = "user@example.com")
		String email,

		@Schema(description = "로컬 개발 환경에서만 응답하는 인증번호", example = "123456", nullable = true)
		String verificationCode,

		@Schema(description = "인증번호 만료 시각")
		LocalDateTime expiresAt
) {
}
