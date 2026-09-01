package com.example.peerfolio.domain.chatmessage.repository;

import com.example.peerfolio.domain.chatmessage.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	@Modifying(flushAutomatically = true)
	@Query("""
			delete from ChatMessage cm
			where cm.user.id = :userId
			   or cm.analysisResult.user.id = :userId
			""")
	int deleteAllByUserId(@Param("userId") Long userId);
}
