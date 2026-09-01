package com.example.peerfolio.domain.auth.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.peerfolio.domain.emailverification.repository.EmailVerificationRepository;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

	private static final String EMAIL = "test@example.com";
	private static final String PASSWORD = "test1234!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailVerificationRepository emailVerificationRepository;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		emailVerificationRepository.deleteAll();
	}

	@Test
	void emailCodeRequestFailsWhenEmailFormatIsInvalid() throws Exception {
		mockMvc.perform(post("/api/v1/auth/email-code")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "invalid-email"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void signupLoginMeAndLogoutFlowWorks() throws Exception {
		// given: 이메일 인증번호를 발급한다.
		requestEmailCode();
		String verificationCode = emailVerificationRepository.findByEmail(EMAIL)
				.orElseThrow()
				.getCode();

		// when: 발급된 인증번호로 회원가입한다.
		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "사용자",
								  "email": "%s",
								  "verificationCode": "%s",
								  "password": "%s"
								}
								""".formatted(EMAIL, verificationCode, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.email").value(EMAIL))
				.andExpect(jsonPath("$.result.nickname").value(notNullValue()));

		// then: 가입 완료된 이메일로는 인증번호를 다시 발급할 수 없다.
		mockMvc.perform(post("/api/v1/auth/email-code")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s"
								}
								""".formatted(EMAIL)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON409"));

		// when: 로그인하고 access token을 발급받는다.
		String accessToken = login();
		String authorization = "Bearer " + accessToken;

		// then: access token으로 내 정보를 조회할 수 있다.
		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.email").value(EMAIL));

		// when: 로그아웃한다.
		mockMvc.perform(post("/api/v1/auth/logout")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true));

		// then: 로그아웃된 access token으로는 보호 API에 접근할 수 없다.
		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	@Test
	void protectedApiReturnsApiResponseWhenTokenIsMissing() throws Exception {
		mockMvc.perform(get("/api/v1/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	@Test
	void signupFailsWhenVerificationCodeIsInvalid() throws Exception {
		requestEmailCode();

		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "사용자",
								  "email": "%s",
								  "verificationCode": "000000",
								  "password": "%s"
								}
								""".formatted(EMAIL, PASSWORD)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("AUTH_400_2"))
				.andExpect(jsonPath("$.message").value("인증번호가 올바르지 않습니다."));
	}

	@Test
	void signupFailsWhenVerificationCodeWasNotIssued() throws Exception {
		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "사용자",
								  "email": "%s",
								  "verificationCode": "123456",
								  "password": "%s"
								}
								""".formatted(EMAIL, PASSWORD)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("AUTH_404_1"))
				.andExpect(jsonPath("$.message").value("발급된 인증번호가 없습니다. 인증번호를 먼저 발급받아 주세요."));
	}

	@Test
	void signupFailsWhenVerificationCodeIsExpired() throws Exception {
		requestEmailCode();
		emailVerificationRepository.findByEmail(EMAIL)
				.orElseThrow()
				.updateCode("123456", LocalDateTime.now().minusSeconds(1));

		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "사용자",
								  "email": "%s",
								  "verificationCode": "123456",
								  "password": "%s"
								}
								""".formatted(EMAIL, PASSWORD)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("AUTH_400_1"))
				.andExpect(jsonPath("$.message").value("인증번호가 만료되었습니다. 인증번호를 다시 발급받아 주세요."));
	}

	@Test
	void loginFailsWhenPasswordIsInvalid() throws Exception {
		signup();

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Wrong123!"
								}
								""".formatted(EMAIL)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("AUTH_401_1"))
				.andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
	}

	@Test
	void loginFailsWithInvalidCredentialsEvenWhenPasswordFormatIsInvalid() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "1",
								  "password": "2"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("AUTH_401_1"))
				.andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."))
				.andExpect(jsonPath("$.result").doesNotExist());
	}

	@Test
	void signupFailsWhenPasswordFormatIsInvalid() throws Exception {
		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "사용자",
								  "email": "%s",
								  "verificationCode": "123456",
								  "password": "password"
								}
								""".formatted(EMAIL)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void protectedApiReturnsApiResponseWhenTokenIsInvalid() throws Exception {
		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	private void requestEmailCode() throws Exception {
		mockMvc.perform(post("/api/v1/auth/email-code")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s"
								}
								""".formatted(EMAIL)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.email").value(EMAIL))
				.andExpect(jsonPath("$.result.verificationCode").doesNotExist());
	}

	private String login() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(EMAIL, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.accessToken").value(notNullValue()))
				.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.result.accessToken");
	}

	private void signup() throws Exception {
		requestEmailCode();
		String verificationCode = emailVerificationRepository.findByEmail(EMAIL)
				.orElseThrow()
				.getCode();

		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "사용자",
								  "email": "%s",
								  "verificationCode": "%s",
								  "password": "%s"
								}
								""".formatted(EMAIL, verificationCode, PASSWORD)))
				.andExpect(status().isOk());
	}
}
