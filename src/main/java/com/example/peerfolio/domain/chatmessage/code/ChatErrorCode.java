package com.example.peerfolio.domain.chatmessage.code;

import com.example.peerfolio.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {

	ANALYSIS_NOT_FOUND(
			HttpStatus.NOT_FOUND,
			"CHAT_404_1",
			"AI 분석 결과가 없습니다. 먼저 분석을 생성해주세요."
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
