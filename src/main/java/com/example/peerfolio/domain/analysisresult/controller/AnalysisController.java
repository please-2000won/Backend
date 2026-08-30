package com.example.peerfolio.domain.analysisresult.controller;

import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.service.AnalysisService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anlaysis")
@Tag(
        name = "Analysis",
        description = "피어 그룹 생성 및 금융 AI 분석 API"
)
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    @Operation(
            summary = "피어 그룹 및 AI 분석 생성",
            description = """
                    JWT 인증 사용자의 금융정보를 기준으로 피어 그룹을 생성하고,
                    피어 그룹 평균과 비교한 AI 분석 결과를 저장합니다.
                    기존 분석 결과와 피어 매칭은 새 분석이 성공한 경우에만 교체됩니다.
                    """
    )
    public ApiResponse<AnalysisResponse> createAnalysis(
            @AuthenticationPrincipal User user
    ) {
        AnalysisResponse response = analysisService.createAnalysis(user);

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                response
        );
    }

    @GetMapping
    @Operation(
            summary = "최신 AI 분석 결과 조회",
            description = """
                    JWT 인증 사용자의 가장 최근 피어 그룹 평균과
                    AI 분석 결과를 조회합니다.
                    """
    )
    public ApiResponse<AnalysisResponse> getLatestAnalysis(
            @AuthenticationPrincipal User user
    ) {
        AnalysisResponse response = analysisService.getLatestAnalysis(user);

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK,
                response
        );
    }
}
