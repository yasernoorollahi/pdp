package com.datarain.pdp.moderation.entity;

import com.datarain.pdp.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "moderation_cases",
        indexes = {
                @Index(name = "idx_moderation_cases_status", columnList = "status"),
                @Index(name = "idx_moderation_cases_target_type_target_id", columnList = "target_type,target_id"),
                @Index(name = "idx_moderation_cases_created_at", columnList = "created_at")
        })
public class ModerationCase extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ModerationTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationStatus status = ModerationStatus.PENDING;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_category", nullable = false, length = 50)
    private ModerationReasonCategory reasonCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationSource source;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(length = 1000)
    private String comment;
}
