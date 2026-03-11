package com.datarain.pdp.signal.normalization.entity;

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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "daily_behavior_metrics",
        indexes = {
                @Index(name = "idx_daily_behavior_user_date", columnList = "user_id,metric_date")
        })
public class DailyBehaviorMetric extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "energy_score")
    private Double energyScore;

    @Column(name = "energy_score_count")
    private Integer energyScoreCount;

    @Column(name = "motivation_score")
    private Double motivationScore;

    @Column(name = "motivation_score_count")
    private Integer motivationScoreCount;

    @Column(name = "friction_count")
    private Integer frictionCount;

    @Column(name = "social_mentions_count")
    private Integer socialMentionsCount;

    @Column(name = "discipline_events_count")
    private Integer disciplineEventsCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_summary", columnDefinition = "jsonb")
    private JsonNode rawSummary;

    @Column(name = "signal_count")
    private Integer signalCount;

    @Column(name = "last_signal_at")
    private Instant lastSignalAt;
}
