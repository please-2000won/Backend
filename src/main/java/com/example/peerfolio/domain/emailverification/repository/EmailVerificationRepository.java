package com.example.peerfolio.domain.emailverification.repository;

import com.example.peerfolio.domain.emailverification.entity.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

	Optional<EmailVerification> findByEmail(String email);
}
