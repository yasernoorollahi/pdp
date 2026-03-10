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
@Table(name = "user_projects",
        indexes = {
                @Index(name = "idx_user_projects_user", columnList = "user_id"),
                @Index(name = "idx_user_projects_signal", columnList = "signal_id"),
                @Index(name = "idx_user_projects_source_hash", columnList = "source_hash")
        })
public class UserProject extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "project_name", nullable = false, columnDefinition = "TEXT")
    private String projectName;

    @Column(name = "normalized_name")
    private String normalizedName;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "extraction_model", length = 100)
    private String extractionModel;

    @Column(name = "pipeline_version", length = 50)
    private String pipelineVersion;

    @Column(name = "source_hash", length = 128)
    private String sourceHash;
}
