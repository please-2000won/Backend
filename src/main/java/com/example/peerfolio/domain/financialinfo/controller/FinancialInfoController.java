package com.example.peerfolio.domain.financialinfo.controller;

import com.example.peerfolio.domain.financialinfo.dto.request.FinancialInfoRequest;
import com.example.peerfolio.domain.financialinfo.dto.response.FinancialInfoResponse;
import com.example.peerfolio.domain.financialinfo.service.FinancialInfoService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/financial-info")
@Tag(name = "FinancialInfo", description = "내 금융 프로필 및 자산 정보 API")
public class FinancialInfoController {

	private final FinancialInfoService financialInfoService;

	@GetMapping
	@Operation(
			summary = "내 금융 정보 조회",
			description = "JWT 인증 사용자의 금융 프로필과 금융자산 정보를 조회합니다."
	)
	public ApiResponse<FinancialInfoResponse> getMyFinancialInfo(
			@AuthenticationPrincipal User user
	) {
		FinancialInfoResponse response = financialInfoService.getMyFinancialInfo(user);
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
	}

	@PutMapping
	@Operation(
			summary = "내 금융 정보 저장",
			description = "JWT 인증 사용자의 금융 프로필과 금융자산 정보를 최초 입력하거나 수정합니다. 나이대는 저장하지 않고 age 기준으로 추후 계산합니다."
	)
	public ApiResponse<FinancialInfoResponse> upsertMyFinancialInfo(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody FinancialInfoRequest request
	) {
		FinancialInfoResponse response = financialInfoService.upsertMyFinancialInfo(user, request);
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
	}
}
