package com.example.peerfolio.domain.user.repository;

import com.example.peerfolio.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
