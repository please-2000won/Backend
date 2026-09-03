package com.example.peerfolio.domain.chatmessage.service;

import com.example.peerfolio.domain.chatmessage.dto.ChatPromptContext;

public interface ChatAiClient {

	String generateAnswer(
			ChatPromptContext context,
			String message
	);
}
