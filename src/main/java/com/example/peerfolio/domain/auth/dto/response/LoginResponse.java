package com.example.peerfolio.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
		@Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
		String accessToken,

		@Schema(description = "Authorization 헤더에 사용할 토큰 타입", example = "Bearer")
		String tokenType
) {

	public static LoginResponse bearer(String accessToken) {
		return new LoginResponse(accessToken, "Bearer");
	}
}
