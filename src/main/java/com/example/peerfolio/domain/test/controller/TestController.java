package com.example.peerfolio.domain.test.controller;

import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

	@GetMapping
	public ApiResponse<String> test() {
		return ApiResponse.onSuccess(
				GeneralSuccessCode.OK,
				"peerfolio server is running"
		);
	}
}
