package com.example.peerfolio.domain.chatfeedback.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.peerfolio.domain.analysisresult.dto.AnalysisResponse;
import com.example.peerfolio.domain.analysisresult.dto.BenchmarkResult;
import com.example.peerfolio.domain.analysisresult.dto.InvestmentBenchmark;
import com.example.peerfolio.domain.analysisresult.dto.PeerProfileBenchmark;
import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.chatfeedback.entity.ChatFeedback;
import com.example.peerfolio.domain.chatfeedback.enums.ChatFeedbackRating;
import com.example.peerfolio.domain.chatfeedback.repository.ChatFeedbackRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.security.JwtProvider;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ChatFeedbackControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AnalysisResultRepository analysisResultRepository;

	@Autowired
	private ChatFeedbackRepository chatFeedbackRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	private User user;
	private String authorization;

	@BeforeEach
	void setUp() {
		chatFeedbackRepository.deleteAll();
		analysisResultRepository.deleteAll();
		userRepository.deleteAll();

		user = userRepository.save(User.create(
				"Test User",
				"chat-feedback@example.com",
				"encoded-password",
				"chat-feedback-user"
		));
		authorization = "Bearer " + jwtProvider.createAccessToken(user.getId());
	}

	@Test
	void chatFeedbackApiReturnsUnauthorizedWhenTokenIsMissing()
			throws Exception {
		mockMvc.perform(post("/api/v1/chat/feedback")
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								UUID.randomUUID().toString(),
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"LIKE",
								"도움이 되는 답변이었어요."
						)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	@Test
	void createFeedbackSavesChatFeedback() throws Exception {
		AnalysisResult analysisResult = saveAnalysisResult();
		String responseId = UUID.randomUUID().toString();

		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								responseId,
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"LIKE",
								"도움이 되는 답변이었어요."
						)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.feedbackId").exists());

		assertThat(chatFeedbackRepository.findAll())
				.hasSize(1)
				.first()
				.satisfies(feedback -> {
					assertThat(feedback.getUserId())
							.isEqualTo(user.getId());
					assertThat(feedback.getAnalysisResultId())
							.isEqualTo(analysisResult.getId());
					assertThat(feedback.getResponseId())
							.isEqualTo(responseId);
					assertThat(feedback.getMessage())
							.isEqualTo("내 자산 배분은 또래와 비교해서 어떤가요?");
					assertThat(feedback.getAnswer())
							.isEqualTo("예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.");
					assertThat(feedback.getRating())
							.isEqualTo(ChatFeedbackRating.LIKE);
					assertThat(feedback.getComment())
							.isEqualTo("도움이 되는 답변이었어요.");
					assertThat(feedback.getCreatedAt())
							.isNotNull();
				});
	}

	@Test
	void createFeedbackFailsWhenAnalysisResultDoesNotExist()
			throws Exception {
		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								UUID.randomUUID().toString(),
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"LIKE",
								"도움이 되는 답변이었어요."
						)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("CHAT_404_1"));
	}

	@Test
	void createFeedbackFailsWhenFeedbackAlreadyExists()
			throws Exception {
		AnalysisResult analysisResult = saveAnalysisResult();
		String responseId = UUID.randomUUID().toString();
		chatFeedbackRepository.save(ChatFeedback.create(
				user.getId(),
				analysisResult.getId(),
				responseId,
				"내 자산 배분은 또래와 비교해서 어떤가요?",
				"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
				ChatFeedbackRating.LIKE,
				"도움이 되는 답변이었어요."
		));

		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								responseId,
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"DISLIKE",
								"중복 피드백입니다."
						)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("CHAT_FEEDBACK_409_1"));
	}

	@Test
	void createFeedbackFailsWhenResponseIdIsInvalid()
			throws Exception {
		saveAnalysisResult();

		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								"invalid-response-id",
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"LIKE",
								"도움이 되는 답변이었어요."
						)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void createFeedbackFailsWhenMessageIsBlank() throws Exception {
		saveAnalysisResult();

		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								UUID.randomUUID().toString(),
								" ",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"LIKE",
								"도움이 되는 답변이었어요."
						)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void createFeedbackFailsWhenAnswerIsBlank() throws Exception {
		saveAnalysisResult();

		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								UUID.randomUUID().toString(),
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								" ",
								"LIKE",
								"도움이 되는 답변이었어요."
						)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void createFeedbackFailsWhenRatingIsInvalid() throws Exception {
		saveAnalysisResult();

		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								UUID.randomUUID().toString(),
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"GOOD",
								"도움이 되는 답변이었어요."
						)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void createFeedbackFailsWhenCommentExceedsLimit()
			throws Exception {
		saveAnalysisResult();

		mockMvc.perform(post("/api/v1/chat/feedback")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createFeedbackRequest(
								UUID.randomUUID().toString(),
								"내 자산 배분은 또래와 비교해서 어떤가요?",
								"예금과 채권 비중이 또래 평균보다 높아 안정적인 편입니다.",
								"LIKE",
								"a".repeat(301)
						)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	private AnalysisResult saveAnalysisResult() throws Exception {
		BenchmarkResult benchmarkResult = new BenchmarkResult(
				new PeerProfileBenchmark(
						2600000L,
						15000000L
				),
				new InvestmentBenchmark(
						10,
						5000000L,
						4000000L,
						3000000L,
						1000000L,
						38.5,
						30.8,
						23.1,
						7.6
				)
		);
		AnalysisResponse.RiskResult riskResult =
				new AnalysisResponse.RiskResult(
						"LOW",
						"안정적인 자산 배분 비중이 높습니다."
				);

		return analysisResultRepository.save(AnalysisResult.create(
				user,
				10,
				objectMapper.writeValueAsString(benchmarkResult),
				objectMapper.writeValueAsString(riskResult),
				35,
				"또래보다 안정적인 자산 배분을 가지고 있습니다."
		));
	}

	private String createFeedbackRequest(
			String responseId,
			String message,
			String answer,
			String rating,
			String comment
	) {
		return """
				{
				  "responseId": "%s",
				  "message": "%s",
				  "answer": "%s",
				  "rating": "%s",
				  "comment": "%s"
				}
				""".formatted(
				responseId,
				message,
				answer,
				rating,
				comment
		);
	}
}
