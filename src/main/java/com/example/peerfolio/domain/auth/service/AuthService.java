package com.example.peerfolio.domain.auth.service;

import com.example.peerfolio.domain.auth.dto.request.EmailCodeRequest;
import com.example.peerfolio.domain.auth.dto.request.LoginRequest;
import com.example.peerfolio.domain.auth.dto.request.SignupRequest;
import com.example.peerfolio.domain.auth.dto.response.EmailCodeResponse;
import com.example.peerfolio.domain.auth.dto.response.LoginResponse;
import com.example.peerfolio.domain.auth.dto.response.SignupResponse;
import com.example.peerfolio.domain.emailverification.entity.EmailVerification;
import com.example.peerfolio.domain.emailverification.repository.EmailVerificationRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.apiPayload.code.GeneralErrorCode;
import com.example.peerfolio.global.apiPayload.exception.ProjectException;
import com.example.peerfolio.global.security.JwtProvider;
import com.example.peerfolio.global.util.NicknameGenerator;
import com.example.peerfolio.global.util.VerificationCodeGenerator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private static final long EMAIL_CODE_EXPIRATION_MINUTES = 5;

	private final UserRepository userRepository;
	private final EmailVerificationRepository emailVerificationRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final NicknameGenerator nicknameGenerator;
	private final VerificationCodeGenerator verificationCodeGenerator;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ProjectException(GeneralErrorCode.CONFLICT);
		}

		String encodedPassword = passwordEncoder.encode(request.password());
		String nickname = generateUniqueNickname();
		User user = User.create(
				request.name(),
				request.email(),
				encodedPassword,
				nickname
		);

		User savedUser = userRepository.save(user);
		return SignupResponse.from(savedUser);
	}

	@Transactional
	public EmailCodeResponse sendEmailCode(EmailCodeRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ProjectException(GeneralErrorCode.CONFLICT);
		}

		String code = verificationCodeGenerator.generate();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EMAIL_CODE_EXPIRATION_MINUTES);
		EmailVerification emailVerification = emailVerificationRepository.findByEmail(request.email())
				.map(existing -> {
					existing.updateCode(code, expiresAt);
					return existing;
				})
				.orElseGet(() -> EmailVerification.create(request.email(), code, expiresAt));

		emailVerificationRepository.save(emailVerification);
		return new EmailCodeResponse(request.email(), code, expiresAt);
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new ProjectException(GeneralErrorCode.UNAUTHORIZED));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ProjectException(GeneralErrorCode.UNAUTHORIZED);
		}

		String accessToken = jwtProvider.createAccessToken(user.getId());
		return LoginResponse.bearer(accessToken);
	}

	private String generateUniqueNickname() {
		for (int i = 0; i < 20; i++) {
			String nickname = nicknameGenerator.generate();

			if (!userRepository.existsByNickname(nickname)) {
				return nickname;
			}
		}

		throw new ProjectException(GeneralErrorCode.INTERNAL_SERVER_ERROR);
	}
}
