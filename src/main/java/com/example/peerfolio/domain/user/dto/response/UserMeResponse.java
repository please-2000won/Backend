package com.example.peerfolio.domain.user.dto.response;

import com.example.peerfolio.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 조회 응답")
public record UserMeResponse(
		@Schema(description = "사용자 ID", example = "1")
		Long userId,

		@Schema(description = "사용자 이름", example = "사용자")
		String name,

		@Schema(description = "사용자 이메일", example = "user@example.com")
		String email,

		@Schema(description = "서버에서 자동 생성된 닉네임", example = "성실한고양이042")
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
