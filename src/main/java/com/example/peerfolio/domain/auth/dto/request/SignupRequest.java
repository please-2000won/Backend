package com.example.peerfolio.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
		@NotBlank(message = "이름은 필수입니다.")
		@Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
		String name,

		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		@Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
		String email,

		@NotBlank(message = "인증번호는 필수입니다.")
		@Pattern(regexp = "\\d{6}", message = "인증번호는 숫자 6자리여야 합니다.")
		String verificationCode,

		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
		@Pattern(regexp = ".*[A-Za-z].*", message = "비밀번호는 영문을 1개 이상 포함해야 합니다.")
		@Pattern(regexp = ".*\\d.*", message = "비밀번호는 숫자를 1개 이상 포함해야 합니다.")
		@Pattern(regexp = ".*[^A-Za-z0-9\\s].*", message = "비밀번호는 특수문자를 1개 이상 포함해야 합니다.")
		String password
) {
}
