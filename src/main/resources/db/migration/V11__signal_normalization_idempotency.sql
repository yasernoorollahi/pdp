-- =====================================================
-- V11__signal_normalization_idempotency.sql
-- Add source_hash for idempotent normalization
-- =====================================================

ALTER TABLE user_entities
    ADD COLUMN IF NOT EXISTS source_hash TEXT;

ALTER TABLE user_preferences
    ADD COLUMN IF NOT EXISTS source_hash TEXT;

ALTER TABLE cognitive_states
    ADD COLUMN IF NOT EXISTS source_hash TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_entities_source_hash
    ON user_entities(source_hash);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_activities_source_hash
    ON user_activities(source_hash);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_topics_source_hash
    ON user_topics(source_hash);

CREATE UNIQUE INDEX IF NOT EXISTS ux_intent_items_source_hash
    ON intent_items(source_hash);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_preferences_source_hash
    ON user_preferences(source_hash);

CREATE UNIQUE INDEX IF NOT EXISTS ux_cognitive_states_source_hash
    ON cognitive_states(source_hash);
