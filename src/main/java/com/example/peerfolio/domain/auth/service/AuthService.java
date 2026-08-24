package com.example.peerfolio.domain.auth.service;

import com.example.peerfolio.domain.auth.dto.request.SignupRequest;
import com.example.peerfolio.domain.auth.dto.response.SignupResponse;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ProjectException(GeneralErrorCode.CONFLICT);
		}

		String encodedPassword = passwordEncoder.encode(request.password());
		User user = User.create(
				request.name(),
				request.email(),
				encodedPassword,
				request.nickname()
		);

		User savedUser = userRepository.save(user);
		return SignupResponse.from(savedUser);
	}
}
