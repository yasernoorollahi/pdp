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
@Table(name = "cognitive_language_items",
        indexes = {
                @Index(name = "idx_cognitive_language_items_user", columnList = "user_id"),
                @Index(name = "idx_cognitive_language_items_type", columnList = "language_type"),
                @Index(name = "idx_cognitive_language_items_signal", columnList = "signal_id"),
                @Index(name = "idx_cognitive_language_items_source_hash", columnList = "source_hash")
        })
public class CognitiveLanguageItem extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message_id")
    private UUID messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_type", nullable = false, length = 50)
    private CognitiveLanguageType languageType;

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
