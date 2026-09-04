package com.example.peerfolio.domain.chatfeedback.repository;

import com.example.peerfolio.domain.chatfeedback.entity.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, Long> {

	boolean existsByUserIdAndResponseId(
			Long userId,
			String responseId
	);

	@Modifying(flushAutomatically = true)
	@Query("""
			delete from ChatFeedback cf
			where cf.userId = :userId
			""")
	int deleteAllByUserId(@Param("userId") Long userId);
}
