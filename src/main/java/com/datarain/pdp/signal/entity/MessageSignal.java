package com.datarain.pdp.signal.entity;

import com.datarain.pdp.common.AuditableEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "message_signals",
        indexes = {
                @Index(name = "idx_message_signals_user", columnList = "user_id"),
                @Index(name = "idx_message_signals_message", columnList = "message_id")
        })
public class MessageSignal extends AuditableEntity {

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "signal_version", nullable = false)
    private Integer signalVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signals", nullable = false, columnDefinition = "jsonb")
    private JsonNode signals;

    @Column(name = "extractor_model", length = 100)
    private String extractorModel;

    @Column(name = "extraction_latency_ms")
    private Integer extractionLatencyMs;

    @Column(name = "pipeline_version", length = 50)
    private String pipelineVersion;
}
