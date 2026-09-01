package com.example.peerfolio.domain.emailverification.service;

import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoOpEmailSender implements EmailSender {

	@Override
	public void sendVerificationCode(
			String to,
			String code,
			LocalDateTime expiresAt
	) {
	}
}
