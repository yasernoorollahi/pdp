package com.datarain.pdp.signal.normalization.entity;

import com.datarain.pdp.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_tone_states",
        indexes = {
                @Index(name = "idx_tone_states_user", columnList = "user_id"),
                @Index(name = "idx_tone_states_signal", columnList = "signal_id"),
                @Index(name = "idx_tone_states_source_hash", columnList = "source_hash")
        })
public class ToneState extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "sentiment", length = 50)
    private String sentiment;

    @Column(name = "mood", columnDefinition = "TEXT")
    private String mood;

    @Column(name = "motivation_level", length = 50)
    private String motivationLevel;

    @Column(name = "effort_perception", length = 50)
    private String effortPerception;

    @Column(name = "friction_detected")
    private Boolean frictionDetected;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "extraction_model", length = 100)
    private String extractionModel;

    @Column(name = "pipeline_version", length = 50)
    private String pipelineVersion;

    @Column(name = "source_hash", length = 128)
    private String sourceHash;
}
