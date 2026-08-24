package com.example.peerfolio.domain.user.controller;

import com.example.peerfolio.domain.user.dto.response.UserMeResponse;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	@GetMapping("/me")
	public ApiResponse<UserMeResponse> getMe(
			@AuthenticationPrincipal User user
	) {
		return ApiResponse.onSuccess(
				GeneralSuccessCode.OK,
				UserMeResponse.from(user)
		);
	}
}
