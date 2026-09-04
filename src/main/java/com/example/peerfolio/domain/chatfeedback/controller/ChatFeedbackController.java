package com.example.peerfolio.domain.chatfeedback.controller;

import com.example.peerfolio.domain.chatfeedback.dto.request.ChatFeedbackRequest;
import com.example.peerfolio.domain.chatfeedback.dto.response.ChatFeedbackResponse;
import com.example.peerfolio.domain.chatfeedback.service.ChatFeedbackService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/feedback")
@Tag(name = "ChatFeedback", description = "챗봇 답변 피드백 API")
public class ChatFeedbackController {

	private final ChatFeedbackService chatFeedbackService;

	@PostMapping
	@Operation(
			summary = "챗봇 답변 피드백 저장",
			description = """
					JWT 인증 사용자가 챗봇 답변에 대해 남긴 좋아요/싫어요 피드백을 저장합니다.
					챗봇 대화 전체는 저장하지 않고, 피드백된 질문과 답변만 저장합니다.
					"""
	)
	public ApiResponse<ChatFeedbackResponse> createFeedback(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody ChatFeedbackRequest request
	) {
		ChatFeedbackResponse response =
				chatFeedbackService.createFeedback(
						user,
						request
				);

		return ApiResponse.onSuccess(
				GeneralSuccessCode.OK,
				response
		);
	}
}
