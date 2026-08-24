package com.example.peerfolio.global.apiPayload.exception.handler;

import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.BaseErrorCode;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    // 프로젝트에서 발생한 예외 처리
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberException(
            ProjectException e
    ) {
        BaseErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(GeneralErrorCode.BAD_REQUEST.getMessage());

        return ResponseEntity.status(GeneralErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.BAD_REQUEST, message));
    }

    // 그 외의 정의되지 않은 모든 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        return ResponseEntity.status(GeneralErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.BAD_REQUEST, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(
            Exception ex
    ) {
        log.error("처리되지 않은 예외가 발생했습니다.", ex);

        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(
                                code,
                                null
                        )
                );
    }
}
