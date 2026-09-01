package com.example.peerfolio.domain.emailverification.service;

import com.example.peerfolio.domain.auth.code.AuthErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

	private static final String VERIFICATION_SUBJECT = "[Peerfolio] 이메일 인증번호 안내";

	private final JavaMailSender javaMailSender;

	@Value("${app.mail.from:${spring.mail.username}}")
	private String from;

	@Override
	public void sendVerificationCode(
			String to,
			String code,
			LocalDateTime expiresAt
	) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(VERIFICATION_SUBJECT);
			helper.setText(createVerificationHtml(code), true);

			javaMailSender.send(message);
		} catch (MessagingException | MailException e) {
			log.error("인증 메일 전송에 실패했습니다.", e);
			throw new ProjectException(AuthErrorCode.EMAIL_SEND_FAILED);
		}
	}

	private String createVerificationHtml(String code) {
		return """
				<div style="font-family:'Apple SD Gothic Neo','Malgun Gothic',Arial,sans-serif;padding:24px;color:#111827;">
					<h2 style="margin:0 0 28px;font-size:28px;font-weight:700;">Peerfolio 이메일 인증</h2>
					<p style="margin:0 0 24px;font-size:16px;line-height:1.6;">아래 인증번호를 <strong>5분 이내</strong>에 입력해 주세요.</p>
					<div style="margin:0 0 28px;font-size:44px;font-weight:800;letter-spacing:8px;line-height:1.2;">%s</div>
					<p style="margin:0;color:#6b7280;font-size:14px;line-height:1.6;">본 메일은 회원가입 이메일 인증을 위해 발송되었습니다.</p>
				</div>
				""".formatted(code);
	}
}
