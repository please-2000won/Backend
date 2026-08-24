package com.example.peerfolio.domain.auth.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.peerfolio.domain.emailverification.repository.EmailVerificationRepository;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
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
		requestEmailCode();
		String verificationCode = emailVerificationRepository.findByEmail(EMAIL)
				.orElseThrow()
				.getCode();

		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "홍진우",
								  "email": "%s",
								  "verificationCode": "%s",
								  "password": "%s"
								}
								""".formatted(EMAIL, verificationCode, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.email").value(EMAIL))
				.andExpect(jsonPath("$.result.nickname").value(notNullValue()));

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

		String accessToken = login();
		String authorization = "Bearer " + accessToken;

		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.email").value(EMAIL));

		mockMvc.perform(post("/api/v1/auth/logout")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true));

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
}
