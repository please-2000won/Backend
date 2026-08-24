package com.example.peerfolio.domain.auth.controller;

import com.example.peerfolio.domain.auth.dto.request.SignupRequest;
import com.example.peerfolio.domain.auth.dto.response.SignupResponse;
import com.example.peerfolio.domain.auth.service.AuthService;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ApiResponse<SignupResponse> signup(
			@RequestBody SignupRequest request
	) {
		SignupResponse response = authService.signup(request);
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
	}
}
