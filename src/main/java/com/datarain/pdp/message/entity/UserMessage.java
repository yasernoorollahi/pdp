package com.datarain.pdp.message.entity;

import com.datarain.pdp.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_messages",
        indexes = {
                @Index(name = "idx_user_messages_user_date", columnList = "user_id,message_date"),
                @Index(name = "idx_user_messages_processed", columnList = "processed")
        })
public class UserMessage extends AuditableEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Column(name = "message_date", nullable = false)
    private LocalDate messageDate;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 20)
    private MessageAnalysisStatus analysisStatus = MessageAnalysisStatus.PENDING;

    @Column(name = "signal_score")
    private Double signalScore;

    @Column(name = "signal_decision", length = 20)
    private String signalDecision;

    @Column(name = "signal_reason", columnDefinition = "TEXT")
    private String signalReason;

    @Column(name = "processed_at")
    private Instant processedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private MessageProcessingStatus processingStatus = MessageProcessingStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;
}
