package com.example.peerfolio.domain.chatmessage.service;

import com.example.peerfolio.domain.chatmessage.dto.ChatPromptContext;
import com.example.peerfolio.domain.chatmessage.dto.request.ChatRequest;
import com.example.peerfolio.domain.chatmessage.dto.response.ChatResponse;
import com.example.peerfolio.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatContextQueryService chatContextQueryService;
	private final ChatAiClient chatAiClient;

	public ChatResponse createAnswer(
			User user,
			ChatRequest request
	) {
		ChatPromptContext context =
				chatContextQueryService.getPromptContext(
						user.getId()
				);

		String answer = chatAiClient.generateAnswer(
				context,
				request.message()
		);

		return new ChatResponse(answer);
	}
}
