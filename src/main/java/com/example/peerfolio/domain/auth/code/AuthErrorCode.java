package com.example.peerfolio.domain.auth.code;

import com.example.peerfolio.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

	INVALID_CREDENTIALS(
			HttpStatus.UNAUTHORIZED,
			"AUTH_401_1",
			"이메일 또는 비밀번호가 올바르지 않습니다."
	),
	EMAIL_CODE_EXPIRED(
			HttpStatus.BAD_REQUEST,
			"AUTH_400_1",
			"인증번호가 만료되었습니다. 인증번호를 다시 발급받아 주세요."
	),
	EMAIL_CODE_MISMATCH(
			HttpStatus.BAD_REQUEST,
			"AUTH_400_2",
			"인증번호가 올바르지 않습니다."
	),
	EMAIL_CODE_ALREADY_USED(
			HttpStatus.BAD_REQUEST,
			"AUTH_400_3",
			"이미 사용된 인증번호입니다."
	),
	EMAIL_CODE_NOT_FOUND(
			HttpStatus.NOT_FOUND,
			"AUTH_404_1",
			"발급된 인증번호가 없습니다. 인증번호를 먼저 발급받아 주세요."
	),
	EMAIL_SEND_FAILED(
			HttpStatus.SERVICE_UNAVAILABLE,
			"AUTH_503_1",
			"인증 메일 전송에 실패했습니다."
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
