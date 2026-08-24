package com.example.peerfolio.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청")
public record LoginRequest(
		@Schema(description = "로그인 이메일", example = "test@example.com")
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@Schema(description = "비밀번호. 8~20자이며 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.", example = "test1234!")
		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
		@Pattern(regexp = ".*[A-Za-z].*", message = "비밀번호는 영문을 1개 이상 포함해야 합니다.")
		@Pattern(regexp = ".*\\d.*", message = "비밀번호는 숫자를 1개 이상 포함해야 합니다.")
		@Pattern(regexp = ".*[^A-Za-z0-9\\s].*", message = "비밀번호는 특수문자를 1개 이상 포함해야 합니다.")
		String password
) {
}
