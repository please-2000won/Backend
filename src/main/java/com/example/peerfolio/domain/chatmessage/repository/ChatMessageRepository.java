package com.example.peerfolio.domain.chatmessage.repository;

import com.example.peerfolio.domain.chatmessage.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
