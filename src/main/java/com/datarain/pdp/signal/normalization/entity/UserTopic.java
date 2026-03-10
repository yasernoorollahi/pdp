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
@Table(name = "user_topics",
        indexes = {
                @Index(name = "idx_user_topics_user", columnList = "user_id"),
                @Index(name = "idx_user_topics_source_hash", columnList = "source_hash")
        })
public class UserTopic extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "topic", columnDefinition = "TEXT")
    private String topic;

    @Column(name = "domain", columnDefinition = "TEXT")
    private String domain;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "source_hash", length = 128)
    private String sourceHash;

    @Column(name = "extraction_model", length = 100)
    private String extractionModel;

    @Column(name = "pipeline_version", length = 50)
    private String pipelineVersion;
}
