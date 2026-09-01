package com.example.peerfolio.domain.emailverification.service;

import java.time.LocalDateTime;

public interface EmailSender {

	void sendVerificationCode(
			String to,
			String code,
			LocalDateTime expiresAt
	);
}
