package com.example.peerfolio.domain.analysisresult.code;

import com.example.peerfolio.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AnalysisErrorCode implements BaseErrorCode {
    ANALYSIS_IN_PROGRESS(
            HttpStatus.CONFLICT,
            "ANALYSIS_409_1",
            "동일한 금융정보에 대한 분석이 진행 중입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
