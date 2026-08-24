package com.example.peerfolio.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증번호 발급 요청")
public record EmailCodeRequest(
		@Schema(description = "인증번호를 발급받을 이메일", example = "test@example.com")
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		@Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
		String email
) {
}
