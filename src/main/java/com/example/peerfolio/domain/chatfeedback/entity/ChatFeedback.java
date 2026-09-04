package com.example.peerfolio.domain.chatfeedback.entity;

import com.example.peerfolio.domain.chatfeedback.enums.ChatFeedbackRating;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "chat_feedback",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_chat_feedback_user_response",
						columnNames = {"user_id", "response_id"}
				)
		}
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatFeedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_feedback_id")
	private Long id;

	@Column(name = "response_id", nullable = false, length = 36)
	private String responseId;

	@Column(nullable = false, length = 500)
	private String message;

	@Column(nullable = false, length = 1200)
	private String answer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ChatFeedbackRating rating;

	@Column(length = 300)
	private String comment;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long analysisResultId;

	public static ChatFeedback create(
			Long userId,
			Long analysisResultId,
			String responseId,
			String message,
			String answer,
			ChatFeedbackRating rating,
			String comment
	) {
		return ChatFeedback.builder()
				.userId(userId)
				.analysisResultId(analysisResultId)
				.responseId(responseId)
				.message(message)
				.answer(answer)
				.rating(rating)
				.comment(comment)
				.createdAt(LocalDateTime.now())
				.build();
	}
}
