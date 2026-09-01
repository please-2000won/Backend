package com.example.peerfolio.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisResult;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.chatmessage.entity.ChatMessage;
import com.example.peerfolio.domain.chatmessage.enums.SenderType;
import com.example.peerfolio.domain.chatmessage.repository.ChatMessageRepository;
import com.example.peerfolio.domain.emailverification.entity.EmailVerification;
import com.example.peerfolio.domain.emailverification.repository.EmailVerificationRepository;
import com.example.peerfolio.domain.financialasset.entity.FinancialAsset;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.entity.FinancialProfile;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.peermatch.entity.PeerMatch;
import com.example.peerfolio.domain.peermatch.repository.PeerMatchRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.security.JwtProvider;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

	private static final String EMAIL = "delete-account@example.com";
	private static final String PASSWORD = "test1234!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FinancialProfileRepository financialProfileRepository;

	@Autowired
	private FinancialAssetRepository financialAssetRepository;

	@Autowired
	private AnalysisResultRepository analysisResultRepository;

	@Autowired
	private PeerMatchRepository peerMatchRepository;

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	@Autowired
	private EmailVerificationRepository emailVerificationRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtProvider jwtProvider;

	private User user;
	private String authorization;

	@BeforeEach
	void setUp() {
		chatMessageRepository.deleteAll();
		peerMatchRepository.deleteAll();
		analysisResultRepository.deleteAll();
		financialAssetRepository.deleteAll();
		financialProfileRepository.deleteAll();
		emailVerificationRepository.deleteAll();
		userRepository.deleteAll();

		user = userRepository.save(User.create(
				"사용자",
				EMAIL,
				passwordEncoder.encode(PASSWORD),
				"탈퇴테스트닉네임001"
		));
		authorization = "Bearer " + jwtProvider.createAccessToken(user.getId());
	}

	@Test
	void deleteAccountFailsWhenPasswordIsInvalid() throws Exception {
		mockMvc.perform(delete("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "password": "Wrong123!"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("USER_401_1"))
				.andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."));

		assertThat(userRepository.findById(user.getId())).isPresent();
	}

	@Test
	void deleteAccountDeletesUserAndRelatedData() throws Exception {
		// given: 탈퇴 대상 사용자와 연결된 금융 정보, 분석 결과, 채팅, 피어 매칭, 이메일 인증 정보가 있다.
		User peer = savePeer();
		FinancialProfile financialProfile = financialProfileRepository.save(FinancialProfile.create(
				user,
				24,
				2_500_000L,
				900_000L,
				700_000L,
				12_000_000L,
				3_000_000L
		));
		FinancialAsset financialAsset = financialAssetRepository.save(FinancialAsset.create(
				user,
				6_000_000L,
				3_000_000L,
				2_000_000L,
				1_000_000L
		));
		AnalysisResult analysisResult = analysisResultRepository.save(AnalysisResult.create(
				user,
				1,
				"{}",
				"{}",
				50,
				"분석 결과입니다."
		));
		chatMessageRepository.save(ChatMessage.create(
				SenderType.USER,
				"분석 결과를 설명해줘",
				user,
				analysisResult
		));
		peerMatchRepository.save(PeerMatch.create(0.91, user, peer));
		peerMatchRepository.save(PeerMatch.create(0.82, peer, user));
		emailVerificationRepository.save(EmailVerification.create(
				EMAIL,
				"123456",
				LocalDateTime.now().plusMinutes(5)
		));

		// when: 회원 탈퇴를 요청한다.
		mockMvc.perform(delete("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "password": "%s"
								}
								""".formatted(PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true));

		// then: 사용자와 사용자에게 연결된 데이터가 삭제된다.
		assertThat(userRepository.findById(user.getId())).isEmpty();
		assertThat(userRepository.findById(peer.getId())).isPresent();
		assertThat(financialProfileRepository.findById(financialProfile.getId())).isEmpty();
		assertThat(financialAssetRepository.findById(financialAsset.getId())).isEmpty();
		assertThat(analysisResultRepository.findById(analysisResult.getId())).isEmpty();
		assertThat(chatMessageRepository.count()).isZero();
		assertThat(peerMatchRepository.count()).isZero();
		assertThat(emailVerificationRepository.findByEmail(EMAIL)).isEmpty();
	}

	@Test
	void deletedAccountTokenCannotAccessProtectedApi() throws Exception {
		deleteAccount();

		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	@Test
	void deletedAccountEmailCanSignUpAgain() throws Exception {
		deleteAccount();

		mockMvc.perform(post("/api/v1/auth/email-code")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s"
								}
								""".formatted(EMAIL)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true));

		String verificationCode = emailVerificationRepository.findByEmail(EMAIL)
				.orElseThrow()
				.getCode();

		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "재가입사용자",
								  "email": "%s",
								  "verificationCode": "%s",
								  "password": "%s"
								}
								""".formatted(EMAIL, verificationCode, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.email").value(EMAIL));
	}

	private User savePeer() {
		return userRepository.save(User.create(
				"피어사용자",
				"peer-delete-account@example.com",
				passwordEncoder.encode(PASSWORD),
				"탈퇴테스트닉네임002"
		));
	}

	private void deleteAccount() throws Exception {
		mockMvc.perform(delete("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "password": "%s"
								}
								""".formatted(PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true));
	}
}
