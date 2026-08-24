package com.example.peerfolio.domain.auth.dto.request;

public record SignupRequest(
		String name,
		String email,
		String password,
		String nickname
) {
}
