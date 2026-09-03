package com.example.peerfolio.domain.chatmessage.controller;

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
import com.example.peerfolio.domain.chatmessage.dto.ChatPromptContext;
import com.example.peerfolio.domain.chatmessage.repository.ChatMessageRepository;
import com.example.peerfolio.domain.chatmessage.service.ChatAiClient;
import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AnalysisResultRepository analysisResultRepository;

	@Autowired
	private FinancialProfileRepository financialProfileRepository;

	@Autowired
	private FinancialAssetRepository financialAssetRepository;

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StubChatAiClient chatAiClient;

	private User user;
	private String authorization;

	@BeforeEach
	void setUp() {
		chatMessageRepository.deleteAll();
		analysisResultRepository.deleteAll();
		financialAssetRepository.deleteAll();
		financialProfileRepository.deleteAll();
		userRepository.deleteAll();
		chatAiClient.reset();

		user = userRepository.save(User.create(
				"Test User",
				"chat@example.com",
				"encoded-password",
				"chat-user"
		));
		authorization = "Bearer " + jwtProvider.createAccessToken(user.getId());
	}

	@Test
	void chatApiReturnsUnauthorizedWhenTokenIsMissing() throws Exception {
		mockMvc.perform(post("/api/v1/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": "How is my asset allocation?"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	@Test
	void createAnswerReturnsAiAnswerBasedOnLatestAnalysisAndFinancialInfo()
			throws Exception {
		FinancialProfile financialProfile = financialProfileRepository.save(
				FinancialProfile.create(
						user,
						24,
						2500000L,
						900000L,
						700000L,
						12000000L,
						3000000L
				)
		);
		FinancialAsset financialAsset = financialAssetRepository.save(
				FinancialAsset.create(
						user,
						6000000L,
						3000000L,
						2000000L,
						1000000L
				)
		);
		AnalysisResult analysisResult = saveAnalysisResult();

		mockMvc.perform(post("/api/v1/chat")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": "How is my asset allocation?"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.answer").value("Your deposit and bond allocation is higher than the peer average."));

		assertThat(chatAiClient.lastAnalysisResultId)
				.isEqualTo(analysisResult.getId());
		assertThat(chatAiClient.lastFinancialProfileId)
				.isEqualTo(financialProfile.getId());
		assertThat(chatAiClient.lastFinancialAssetId)
				.isEqualTo(financialAsset.getId());
		assertThat(chatAiClient.lastMessage)
				.isEqualTo("How is my asset allocation?");
		assertThat(chatAiClient.transactionActive)
				.isFalse();
		assertThat(chatMessageRepository.count()).isZero();
	}

	@Test
	void createAnswerFailsWhenAnalysisResultDoesNotExist() throws Exception {
		financialProfileRepository.save(FinancialProfile.create(
				user,
				24,
				2500000L,
				900000L,
				700000L,
				12000000L,
				3000000L
		));
		financialAssetRepository.save(FinancialAsset.create(
				user,
				6000000L,
				3000000L,
				2000000L,
				1000000L
		));

		mockMvc.perform(post("/api/v1/chat")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": "How is my asset allocation?"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("CHAT_404_1"));
	}

	@Test
	void createAnswerFailsWhenFinancialProfileDoesNotExist() throws Exception {
		saveAnalysisResult();

		mockMvc.perform(post("/api/v1/chat")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": "How is my asset allocation?"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON404"));
	}

	@Test
	void createAnswerFailsWhenFinancialAssetDoesNotExist() throws Exception {
		saveAnalysisResult();
		financialProfileRepository.save(FinancialProfile.create(
				user,
				24,
				2500000L,
				900000L,
				700000L,
				12000000L,
				3000000L
		));

		mockMvc.perform(post("/api/v1/chat")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": "How is my asset allocation?"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON404"));
	}

	@Test
	void createAnswerFailsWhenMessageIsBlank() throws Exception {
		mockMvc.perform(post("/api/v1/chat")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": " "
								}
								"""))
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
						"Stable asset allocation is high."
				);

		return analysisResultRepository.save(AnalysisResult.create(
				user,
				10,
				objectMapper.writeValueAsString(benchmarkResult),
				objectMapper.writeValueAsString(riskResult),
				35,
				"Asset allocation is more stable than peers.",
				"test-input-hash"
		));
	}

	@TestConfiguration
	static class ChatTestConfig {

		@Bean
		@Primary
		StubChatAiClient stubChatAiClient() {
			return new StubChatAiClient();
		}
	}

	static class StubChatAiClient implements ChatAiClient {

		private Long lastAnalysisResultId;
		private Long lastFinancialProfileId;
		private Long lastFinancialAssetId;
		private String lastMessage;
		private boolean transactionActive;

		@Override
		public String generateAnswer(
				ChatPromptContext context,
				String message
		) {
			this.lastAnalysisResultId = context.analysisResultId();
			this.lastFinancialProfileId = context.financialProfileId();
			this.lastFinancialAssetId = context.financialAssetId();
			this.lastMessage = message;
			this.transactionActive =
					TransactionSynchronizationManager
							.isActualTransactionActive();

			return "Your deposit and bond allocation is higher than the peer average.";
		}

		private void reset() {
			this.lastAnalysisResultId = null;
			this.lastFinancialProfileId = null;
			this.lastFinancialAssetId = null;
			this.lastMessage = null;
			this.transactionActive = false;
		}
	}
}
