package com.example.peerfolio.domain.auth.dto.request;

public record LoginRequest(
		String email,
		String password
) {
}
