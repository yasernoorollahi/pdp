package com.datarain.pdp.signal.normalization.repository;

import com.datarain.pdp.signal.normalization.entity.DailyBehaviorMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface DailyBehaviorMetricRepository extends JpaRepository<DailyBehaviorMetric, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO daily_behavior_metrics (
                user_id,
                metric_date,
                energy_score,
                energy_score_count,
                social_mentions_count,
                discipline_events_count,
                friction_count,
                motivation_score,
                motivation_score_count,
                raw_summary,
                signal_count,
                last_signal_at
            ) VALUES (
                :userId,
                :metricDate,
                :energyScore,
                CASE WHEN :energyScore IS NULL THEN 0 ELSE 1 END,
                :socialMentions,
                :disciplineEvents,
                :frictionCount,
                :motivationScore,
                CASE WHEN :motivationScore IS NULL THEN 0 ELSE 1 END,
                CAST(:rawSummary AS jsonb),
                1,
                :lastSignalAt
            )
            ON CONFLICT (user_id, metric_date)
            DO UPDATE SET
                energy_score = CASE
                    WHEN EXCLUDED.energy_score IS NULL THEN daily_behavior_metrics.energy_score
                    WHEN daily_behavior_metrics.energy_score IS NULL
                        OR COALESCE(daily_behavior_metrics.energy_score_count, 0) = 0 THEN EXCLUDED.energy_score
                    ELSE (daily_behavior_metrics.energy_score * daily_behavior_metrics.energy_score_count + EXCLUDED.energy_score)
                        / (daily_behavior_metrics.energy_score_count + 1)
                END,
                energy_score_count = CASE
                    WHEN EXCLUDED.energy_score IS NULL THEN COALESCE(daily_behavior_metrics.energy_score_count, 0)
                    ELSE COALESCE(daily_behavior_metrics.energy_score_count, 0) + 1
                END,
                social_mentions_count = COALESCE(daily_behavior_metrics.social_mentions_count, 0) + EXCLUDED.social_mentions_count,
                discipline_events_count = COALESCE(daily_behavior_metrics.discipline_events_count, 0) + EXCLUDED.discipline_events_count,
                friction_count = COALESCE(daily_behavior_metrics.friction_count, 0) + EXCLUDED.friction_count,
                motivation_score = CASE
                    WHEN EXCLUDED.motivation_score IS NULL THEN daily_behavior_metrics.motivation_score
                    WHEN daily_behavior_metrics.motivation_score IS NULL
                        OR COALESCE(daily_behavior_metrics.motivation_score_count, 0) = 0 THEN EXCLUDED.motivation_score
                    ELSE (daily_behavior_metrics.motivation_score * daily_behavior_metrics.motivation_score_count + EXCLUDED.motivation_score)
                        / (daily_behavior_metrics.motivation_score_count + 1)
                END,
                motivation_score_count = CASE
                    WHEN EXCLUDED.motivation_score IS NULL THEN COALESCE(daily_behavior_metrics.motivation_score_count, 0)
                    ELSE COALESCE(daily_behavior_metrics.motivation_score_count, 0) + 1
                END,
                raw_summary = COALESCE(EXCLUDED.raw_summary, daily_behavior_metrics.raw_summary),
                signal_count = COALESCE(daily_behavior_metrics.signal_count, 0) + 1,
                last_signal_at = GREATEST(
                    COALESCE(daily_behavior_metrics.last_signal_at, EXCLUDED.last_signal_at),
                    EXCLUDED.last_signal_at
                ),
                updated_at = now()
            """, nativeQuery = true)
    int upsertMetrics(@Param("userId") UUID userId,
                      @Param("metricDate") LocalDate metricDate,
                      @Param("energyScore") Double energyScore,
                      @Param("socialMentions") int socialMentions,
                      @Param("disciplineEvents") int disciplineEvents,
                      @Param("frictionCount") int frictionCount,
                      @Param("motivationScore") Double motivationScore,
                      @Param("rawSummary") String rawSummary,
                      @Param("lastSignalAt") Instant lastSignalAt);
}
