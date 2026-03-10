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

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_preferences",
        indexes = {
                @Index(name = "idx_user_preferences_user", columnList = "user_id"),
                @Index(name = "idx_user_preferences_source_hash", columnList = "source_hash")
        })
public class UserPreference extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message_id")
    private UUID messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_type", nullable = false, length = 50)
    private PreferenceType preferenceType;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "extraction_model", length = 100)
    private String extractionModel;

    @Column(name = "pipeline_version", length = 50)
    private String pipelineVersion;

    @Column(name = "source_hash", length = 128)
    private String sourceHash;
}
