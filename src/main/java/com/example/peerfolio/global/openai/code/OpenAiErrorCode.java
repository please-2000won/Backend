package com.example.peerfolio.global.openai.code;

import com.example.peerfolio.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OpenAiErrorCode implements BaseErrorCode {

    REQUEST_FAILED(
            HttpStatus.BAD_GATEWAY,
            "OPENAI_502_1",
            "AI 분석 요청에 실패했습니다."
    ),

    INVALID_RESPONSE(
            HttpStatus.BAD_GATEWAY,
            "OPENAI_502_2",
            "AI 분석 응답이 올바르지 않습니다."
    ),

    RESPONSE_REFUSED(
            HttpStatus.BAD_GATEWAY,
            "OPENAI_502_3",
            "AI 분석 요청이 거절되었습니다."
    ),

    REQUEST_TIMEOUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "OPENAI_504_1",
            "AI 분석 요청 시간이 초과되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
