package com.example.peerfolio.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청")
public record DeleteAccountRequest(
		@Schema(description = "현재 비밀번호", example = "Password123!")
		@NotBlank(message = "비밀번호는 필수입니다.")
		String password
) {
}
