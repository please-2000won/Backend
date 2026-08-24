package com.example.peerfolio.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "이메일 인증번호 발급 응답")
public record EmailCodeResponse(
		@Schema(description = "인증번호를 발급한 이메일", example = "test@example.com")
		String email,

		@Schema(description = "인증번호 만료 시각. 인증번호 원문은 응답에 포함하지 않습니다.")
		LocalDateTime expiresAt
) {
}
