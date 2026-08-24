package com.example.peerfolio.domain.auth.controller;

import com.example.peerfolio.domain.auth.dto.request.EmailCodeRequest;
import com.example.peerfolio.domain.auth.dto.request.SignupRequest;
import com.example.peerfolio.domain.auth.dto.request.LoginRequest;
import com.example.peerfolio.domain.auth.dto.response.EmailCodeResponse;
import com.example.peerfolio.domain.auth.dto.response.LoginResponse;
import com.example.peerfolio.domain.auth.dto.response.SignupResponse;
import com.example.peerfolio.domain.auth.service.AuthService;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "회원가입, 로그인, 로그아웃, 이메일 인증번호 API")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	@Operation(
			summary = "회원가입",
			description = "이메일 인증번호 검증 후 회원을 생성합니다. 닉네임은 서버에서 자동 생성됩니다."
	)
	public ApiResponse<SignupResponse> signup(
			@Valid @RequestBody SignupRequest request
	) {
		SignupResponse response = authService.signup(request);
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
	}

	@PostMapping("/email-code")
	@Operation(
			summary = "이메일 인증번호 발급",
			description = "회원가입 전 이메일 인증번호를 발급합니다. 개발 단계에서는 인증번호를 응답으로 반환합니다."
	)
	public ApiResponse<EmailCodeResponse> sendEmailCode(
			@Valid @RequestBody EmailCodeRequest request
	) {
		EmailCodeResponse response = authService.sendEmailCode(request);
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
	}

	@PostMapping("/login")
	@Operation(
			summary = "이메일 로그인",
			description = "이메일과 비밀번호를 검증한 뒤 JWT access token을 발급합니다."
	)
	public ApiResponse<LoginResponse> login(
			@Valid @RequestBody LoginRequest request
	) {
		LoginResponse response = authService.login(request);
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
	}

	@PostMapping("/logout")
	@Operation(
			summary = "로그아웃",
			description = "Stateless JWT 구조이므로 서버 토큰 폐기 없이 성공 응답을 반환합니다. 클라이언트에서 access token을 삭제해야 합니다."
	)
	public ApiResponse<Void> logout() {
		return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
	}
}
