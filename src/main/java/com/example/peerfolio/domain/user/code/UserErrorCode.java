package com.example.peerfolio.domain.user.code;

import com.example.peerfolio.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

	INVALID_PASSWORD(
			HttpStatus.UNAUTHORIZED,
			"USER_401_1",
			"비밀번호가 올바르지 않습니다."
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
