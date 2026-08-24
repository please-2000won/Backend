package com.example.peerfolio.global.util;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {

	public String generate() {
		int code = ThreadLocalRandom.current().nextInt(1_000_000);
		return String.format("%06d", code);
	}
}
