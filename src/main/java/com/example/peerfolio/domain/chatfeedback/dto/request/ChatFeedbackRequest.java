package com.example.peerfolio.domain.chatfeedback.dto.request;

import com.example.peerfolio.domain.chatfeedback.enums.ChatFeedbackRating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatFeedbackRequest(
		@NotBlank(message = "responseId는 필수입니다.")
		@Pattern(
				regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
				message = "responseId 형식이 올바르지 않습니다."
		)
		String responseId,

		@NotBlank(message = "메시지는 필수입니다.")
		// 기존 ChatRequest 500자 제한
		@Size(max = 500, message = "메시지는 500자 이하로 입력해주세요.")
		String message,

		@NotBlank(message = "답변은 필수입니다.")
		// OpenAI 웅덥 스키마 maxLength 1200
		@Size(max = 1200, message = "답변은 1200자 이하로 입력해주세요.")
		String answer,

		@NotNull(message = "피드백 평가는 필수입니다.")
		ChatFeedbackRating rating,

		// 사용자가 직접 남기는 피드백 코멘트로, 너무 길어지는 것 방지용으로 300자 제한
		@Size(max = 300, message = "코멘트는 300자 이하로 입력해주세요.")
		String comment
) {
}
