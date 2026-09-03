package com.example.peerfolio.domain.chatmessage.controller;

import com.example.peerfolio.domain.chatmessage.dto.request.ChatRequest;
import com.example.peerfolio.domain.chatmessage.dto.response.ChatResponse;
import com.example.peerfolio.domain.chatmessage.service.ChatService;
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
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "챗봇 답변 API")
public class ChatController {

	private final ChatService chatService;

	@PostMapping
	@Operation(
			summary = "챗봇 답변 생성",
			description = """
					JWT 인증 사용자의 최신 투자 분석 결과와 금융 프로필, 금융 자산 정보를 기반으로 현재 질문에 대한 AI 답변을 반환합니다.
					대화 내용은 저장하지 않으며, 이전 대화 맥락도 답변 생성에 사용하지 않습니다.
					"""
	)
	public ApiResponse<ChatResponse> createAnswer(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody ChatRequest request
	) {
		ChatResponse response = chatService.createAnswer(
				user,
				request
		);

		return ApiResponse.onSuccess(
				GeneralSuccessCode.OK,
				response
		);
	}
}
