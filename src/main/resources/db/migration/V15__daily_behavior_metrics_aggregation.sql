-- =====================================================
-- V15__daily_behavior_metrics_aggregation.sql
-- Add per-metric counts to support running averages
-- =====================================================

ALTER TABLE daily_behavior_metrics
    ADD COLUMN IF NOT EXISTS energy_score_count INTEGER DEFAULT 0;

ALTER TABLE daily_behavior_metrics
    ADD COLUMN IF NOT EXISTS motivation_score_count INTEGER DEFAULT 0;

UPDATE daily_behavior_metrics
SET energy_score_count = CASE
    WHEN energy_score IS NULL THEN 0
    ELSE 1
END
WHERE energy_score_count IS NULL
   OR energy_score_count = 0;

UPDATE daily_behavior_metrics
SET motivation_score_count = CASE
    WHEN motivation_score IS NULL THEN 0
    ELSE 1
END
WHERE motivation_score_count IS NULL
   OR motivation_score_count = 0;
