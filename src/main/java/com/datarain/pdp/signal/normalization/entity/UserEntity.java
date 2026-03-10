package com.datarain.pdp.signal.normalization.entity;

import com.datarain.pdp.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_entities",
        indexes = {
                @Index(name = "idx_user_entities_user", columnList = "user_id"),
                @Index(name = "idx_user_entities_type", columnList = "entity_type"),
                @Index(name = "idx_user_entities_source_hash", columnList = "source_hash")
        })
public class UserEntity extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "canonical_name")
    private String canonicalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private UserEntityType entityType;

    @Column(name = "first_seen")
    private LocalDate firstSeen;

    @Column(name = "last_seen")
    private LocalDate lastSeen;

    @Column(name = "mention_count", nullable = false)
    private Integer mentionCount = 1;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "source_signal_id")
    private UUID sourceSignalId;

    @Column(name = "extraction_model", length = 100)
    private String extractionModel;

    @Column(name = "pipeline_version", length = 50)
    private String pipelineVersion;

    @Column(name = "source_hash", length = 128)
    private String sourceHash;
}
