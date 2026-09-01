package com.example.peerfolio.domain.emailverification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.example.peerfolio.domain.auth.code.AuthErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

class SmtpEmailSenderTest {

	private static final String FROM = "peerfolio@example.com";
	private static final String TO = "user@example.com";
	private static final String CODE = "123456";
	private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 9, 1, 12, 0);

	private final JavaMailSender javaMailSender = org.mockito.Mockito.mock(JavaMailSender.class);
	private final SmtpEmailSender smtpEmailSender = new SmtpEmailSender(javaMailSender);

	@Test
	void sendVerificationCodeSendsEmailMessage() throws Exception {
		ReflectionTestUtils.setField(smtpEmailSender, "from", FROM);
		MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
		org.mockito.BDDMockito.given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
		ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

		smtpEmailSender.sendVerificationCode(TO, CODE, EXPIRES_AT);

		then(javaMailSender).should().send(captor.capture());
		MimeMessage message = captor.getValue();
		assertThat(message.getFrom()[0].toString()).isEqualTo(FROM);
		assertThat(message.getAllRecipients()[0].toString()).isEqualTo(TO);
		assertThat(message.getSubject()).isEqualTo("[Peerfolio] 이메일 인증번호 안내");
		assertThat(message.getContent().toString())
				.contains("Peerfolio 이메일 인증", "아래 인증번호를", CODE, "본 메일은 회원가입 이메일 인증을 위해 발송되었습니다.");
	}

	@Test
	@DisplayName("메일 발송 실패 시 서비스 사용 불가 에러 코드로 변환한다")
	void sendVerificationCodeThrowsProjectExceptionWhenMailSendFails() {
		ReflectionTestUtils.setField(smtpEmailSender, "from", FROM);
		org.mockito.BDDMockito.given(javaMailSender.createMimeMessage())
				.willReturn(new MimeMessage(Session.getInstance(new Properties())));
		willThrow(new MailSendException("failed"))
				.given(javaMailSender)
				.send(any(MimeMessage.class));

		assertThatThrownBy(() -> smtpEmailSender.sendVerificationCode(TO, CODE, EXPIRES_AT))
				.isInstanceOf(ProjectException.class)
				.extracting("errorCode")
				.isEqualTo(AuthErrorCode.EMAIL_SEND_FAILED);
		assertThat(AuthErrorCode.EMAIL_SEND_FAILED.getStatus().value()).isEqualTo(503);
	}
}
