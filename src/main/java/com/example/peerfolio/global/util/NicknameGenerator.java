package com.example.peerfolio.global.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class NicknameGenerator {

	private static final List<String> ADJECTIVES = List.of(
			"성실한",
			"차분한",
			"빠른",
			"따뜻한",
			"똑똑한",
			"용감한",
			"꾸준한",
			"명랑한",
			"신중한",
			"활발한"
	);

	private static final List<String> ANIMALS = List.of(
			"고양이",
			"강아지",
			"토끼",
			"여우",
			"판다",
			"수달",
			"다람쥐",
			"코알라",
			"펭귄",
			"햄스터"
	);

	public String generate() {
		String adjective = randomElement(ADJECTIVES);
		String animal = randomElement(ANIMALS);
		int number = ThreadLocalRandom.current().nextInt(1000);

		return adjective + animal + String.format("%03d", number);
	}

	private String randomElement(List<String> values) {
		return values.get(ThreadLocalRandom.current().nextInt(values.size()));
	}
}
