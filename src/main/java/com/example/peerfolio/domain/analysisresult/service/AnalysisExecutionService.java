package com.example.peerfolio.domain.analysisresult.service;

import com.example.peerfolio.domain.analysisresult.entity.AnalysisExecution;
import com.example.peerfolio.domain.analysisresult.repository.AnalysisExecutionRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisExecutionService {

    private final AnalysisExecutionRepository analysisExecutionRepository;
    private final UserRepository userRepository;

    // AI 호출 전에 실행 상태를 별도 트랜잭션으로 등록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void claim(
            Long userId,
            String inputHash
    ) {
        User user = userRepository.getReferenceById(userId);

        AnalysisExecution execution =
                AnalysisExecution.create(
                        user,
                        inputHash
                );

        // 즉시 INSERT하여 복합 유니크 제약 충돌 확인
        analysisExecutionRepository.saveAndFlush(execution);
    }

    // AI 호출 성공 또는 실패 후 실행 상태 제거
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(
            Long userId,
            String inputHash
    ) {
        analysisExecutionRepository.deleteByUserIdAndInputHash(
                userId,
                inputHash
        );
    }
}
