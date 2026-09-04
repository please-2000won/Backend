package com.example.peerfolio.domain.chatfeedback.service;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.chatfeedback.code.ChatFeedbackErrorCode;
import com.example.peerfolio.domain.chatfeedback.dto.request.ChatFeedbackRequest;
import com.example.peerfolio.domain.chatfeedback.dto.response.ChatFeedbackResponse;
import com.example.peerfolio.domain.chatfeedback.entity.ChatFeedback;
import com.example.peerfolio.domain.chatfeedback.repository.ChatFeedbackRepository;
import com.example.peerfolio.domain.chatmessage.code.ChatErrorCode;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatFeedbackService {

	private final ChatFeedbackRepository chatFeedbackRepository;
	private final AnalysisResultRepository analysisResultRepository;

	@Transactional
	public ChatFeedbackResponse createFeedback(
			User user,
			ChatFeedbackRequest request
	) {
		if (chatFeedbackRepository.existsByUserIdAndResponseId(
				user.getId(),
				request.responseId()
		)) {
			throw new ProjectException(
					ChatFeedbackErrorCode.DUPLICATE_FEEDBACK
			);
		}

		AnalysisResult analysisResult =
				analysisResultRepository.findByUserId(
						user.getId()
				).orElseThrow(() ->
						new ProjectException(
								ChatErrorCode.ANALYSIS_NOT_FOUND
						)
				);

		ChatFeedback feedback = ChatFeedback.create(
				user.getId(),
				analysisResult.getId(),
				request.responseId(),
				request.message(),
				request.answer(),
				request.rating(),
				request.comment()
		);

		ChatFeedback savedFeedback;

		try {
			savedFeedback = chatFeedbackRepository.saveAndFlush(feedback);
		} catch (DataIntegrityViolationException exception) {
			throw new ProjectException(
					ChatFeedbackErrorCode.DUPLICATE_FEEDBACK
			);
		}

		return new ChatFeedbackResponse(
				savedFeedback.getId()
		);
	}
}
