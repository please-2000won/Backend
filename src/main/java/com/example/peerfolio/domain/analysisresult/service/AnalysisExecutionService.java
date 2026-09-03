package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisExecution;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisExecutionRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalysisExecutionService {

    private final AnalysisExecutionRepository analysisExecutionRepository;
    private final UserRepository userRepository;

    // AI 호출 전에 실행 상태를 별도 트랜잭션으로 등록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long claim(
            Long userId,
            String inputHash
    ) {
        // 동일 입력의 만료된 실행 정보가 있으면 먼저 제거
        analysisExecutionRepository.deleteExpired(
                userId,
                inputHash,
                LocalDateTime.now()
        );

        User user = userRepository.getReferenceById(userId);

        AnalysisExecution execution =
                AnalysisExecution.create(
                        user,
                        inputHash
                );

        return execution.getId();
    }

    // AI 호출 성공 또는 실패 후 실행 상태 제거
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(Long executionId) {
        analysisExecutionRepository.deleteById(executionId);
    }
}
