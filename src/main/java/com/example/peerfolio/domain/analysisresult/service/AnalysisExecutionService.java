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
        LocalDateTime now = LocalDateTime.now();

        // 동일 입력에 대해 만료된 실행 레코드가 있다면 제거
        analysisExecutionRepository.deleteExpired(
                userId,
                inputHash,
                now
        );

        User user = userRepository.getReferenceById(userId);

        AnalysisExecution execution =
                AnalysisExecution.create(
                        user,
                        inputHash
                );

        // INSERT를 즉시 실행해 복합 유니크 제약 충돌을 여기서 감지
        AnalysisExecution savedExecution =
                analysisExecutionRepository.saveAndFlush(execution);

        return savedExecution.getId();
    }

    // AI 호출 성공 또는 실패 후 실행 상태 제거
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(Long executionId) {
        analysisExecutionRepository.deleteById(executionId);
    }
}
