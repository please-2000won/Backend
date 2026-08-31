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
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
