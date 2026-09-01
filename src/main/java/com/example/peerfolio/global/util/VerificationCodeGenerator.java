package com.example.peerfolio.global.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public String generate() {
		int code = SECURE_RANDOM.nextInt(1_000_000);
		return String.format("%06d", code);
	}
}
