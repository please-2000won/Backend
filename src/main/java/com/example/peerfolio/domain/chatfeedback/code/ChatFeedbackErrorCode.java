package com.example.peerfolio.domain.chatfeedback.code;

import com.example.peerfolio.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatFeedbackErrorCode implements BaseErrorCode {

	DUPLICATE_FEEDBACK(
			HttpStatus.CONFLICT,
			"CHAT_FEEDBACK_409_1",
			"이미 피드백을 남긴 챗봇 답변입니다."
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}
