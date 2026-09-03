package com.example.peerfolio.domain.analysisresult.entity;

import com.example.peerfolio.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "analysis_execution",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_execution_user_hash",
                        columnNames = {"user_id", "input_hash"}
                )
        }
)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalysisExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_execution_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "input_hash", nullable = false, length = 64)
    private String inputHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static AnalysisExecution create(
            User user,
            String inputHash
    ) {
        return AnalysisExecution.builder()
                .user(user)
                .inputHash(inputHash)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
