package com.example.peerfolio.domain.user.dto.response;

import com.example.peerfolio.domain.user.entity.User;

public record UserMeResponse(
		Long userId,
		String name,
		String email,
		String nickname
) {

	public static UserMeResponse from(User user) {
		return new UserMeResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getNickname()
		);
	}
}
