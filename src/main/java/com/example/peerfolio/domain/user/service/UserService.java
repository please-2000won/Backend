package com.example.peerfolio.domain.user.service;

import com.example.peerfolio.domain.analysisresult.repository.AnalysisResultRepository;
import com.example.peerfolio.domain.chatmessage.repository.ChatMessageRepository;
import com.example.peerfolio.domain.emailverification.repository.EmailVerificationRepository;
import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.peermatch.repository.PeerMatchRepository;
import com.example.peerfolio.domain.user.code.UserErrorCode;
import com.example.peerfolio.domain.user.dto.request.DeleteAccountRequest;
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
public class UserService {

	private final UserRepository userRepository;
	private final FinancialProfileRepository financialProfileRepository;
	private final FinancialAssetRepository financialAssetRepository;
	private final AnalysisResultRepository analysisResultRepository;
	private final PeerMatchRepository peerMatchRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final EmailVerificationRepository emailVerificationRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void deleteAccount(
			User principal,
			DeleteAccountRequest request
	) {
		User user = userRepository.findByIdForUpdate(principal.getId())
				.orElseThrow(() -> new ProjectException(GeneralErrorCode.UNAUTHORIZED));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ProjectException(UserErrorCode.INVALID_PASSWORD);
		}

		Long userId = user.getId();
		String email = user.getEmail();

		chatMessageRepository.deleteAllByUserId(userId);
		peerMatchRepository.deleteAllByUserId(userId);
		analysisResultRepository.deleteAllByUserId(userId);
		financialAssetRepository.deleteByUserId(userId);
		financialProfileRepository.deleteByUserId(userId);
		emailVerificationRepository.deleteByEmail(email);
		userRepository.delete(user);
	}
}
