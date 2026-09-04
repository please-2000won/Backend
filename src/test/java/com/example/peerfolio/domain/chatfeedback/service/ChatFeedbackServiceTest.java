package com.example.peerfolio.domain.chatfeedback.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.chatfeedback.code.ChatFeedbackErrorCode;
import com.example.peerfolio.domain.chatfeedback.dto.request.ChatFeedbackRequest;
import com.example.peerfolio.domain.chatfeedback.entity.ChatFeedback;
import com.example.peerfolio.domain.chatfeedback.enums.ChatFeedbackRating;
import com.example.peerfolio.domain.chatfeedback.repository.ChatFeedbackRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ChatFeedbackServiceTest {

	@Mock
	private ChatFeedbackRepository chatFeedbackRepository;

	@Mock
	private AnalysisResultRepository analysisResultRepository;

	@InjectMocks
	private ChatFeedbackService chatFeedbackService;

	@Test
	void createFeedbackConvertsDbConstraintViolationToDuplicateFeedbackException() {
		User user = mock(User.class);
		AnalysisResult analysisResult = mock(AnalysisResult.class);
		String responseId = UUID.randomUUID().toString();
		ChatFeedbackRequest request = new ChatFeedbackRequest(
				responseId,
				"내 자산 배분은 또래와 비교해서 어떤가요?",
				"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
				ChatFeedbackRating.LIKE,
				"도움이 되는 답변이었어요."
		);

		when(user.getId())
				.thenReturn(1L);
		when(analysisResult.getId())
				.thenReturn(10L);
		when(chatFeedbackRepository.existsByUserIdAndResponseId(
				user.getId(),
				responseId
		)).thenReturn(false);
		when(analysisResultRepository.findByUserId(user.getId()))
				.thenReturn(Optional.of(analysisResult));
		when(chatFeedbackRepository.saveAndFlush(any(ChatFeedback.class)))
				.thenThrow(new DataIntegrityViolationException("duplicate feedback"));

		assertThatThrownBy(() -> chatFeedbackService.createFeedback(user, request))
				.isInstanceOfSatisfying(ProjectException.class, exception ->
						assertThat(exception.getErrorCode())
								.isEqualTo(ChatFeedbackErrorCode.DUPLICATE_FEEDBACK)
				);
	}
}
