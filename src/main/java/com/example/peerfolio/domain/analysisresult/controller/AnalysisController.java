package com.example.peerfolio.domain.analysisresult.controller;

import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.service.AnalysisService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anlaysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
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
