package com.example.peerfolio.domain.user.repository;

import com.example.peerfolio.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	// 회원가입 시 이메일 중복 확인용
	boolean existsByEmail(String email);

	// 로그인 때 사용 예정
	Optional<User> findByEmail(String email);
}
