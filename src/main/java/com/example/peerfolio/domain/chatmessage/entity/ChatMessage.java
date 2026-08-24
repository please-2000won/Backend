package com.example.peerfolio.domain.chatmessage.entity;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.chatmessage.enums.SenderType;
import com.example.peerfolio.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_message_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SenderType senderType;

	@Lob
	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "analysis_result_id", nullable = false)
	private AnalysisResult analysisResult;

	public static ChatMessage create(
			SenderType senderType,
			String content,
			User user,
			AnalysisResult analysisResult
	) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.senderType = senderType;
		chatMessage.content = content;
		chatMessage.createdAt = LocalDateTime.now();
		chatMessage.user = user;
		chatMessage.analysisResult = analysisResult;
		return chatMessage;
	}
}
