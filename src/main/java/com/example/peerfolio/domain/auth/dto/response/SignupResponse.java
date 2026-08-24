package com.example.peerfolio.domain.auth.dto.response;

import com.example.peerfolio.domain.user.entity.User;

public record SignupResponse(
		Long userId,
		String name,
		String email,
		String nickname
) {

	public static SignupResponse from(User user) {
		return new SignupResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getNickname()
		);
	}
}
