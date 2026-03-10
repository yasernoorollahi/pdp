-- =====================================================
-- V9__signal_normalization_pipeline_hardening.sql
-- Normalize pipeline stability + traceability
-- =====================================================


-- =====================================================
-- 1️⃣ MESSAGE_SIGNALS NORMALIZATION STATE
-- =====================================================

ALTER TABLE message_signals
    ADD COLUMN normalized BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE message_signals
    ADD COLUMN normalization_started_at TIMESTAMPTZ;

ALTER TABLE message_signals
    ADD COLUMN normalized_at TIMESTAMPTZ;

ALTER TABLE message_signals
    ADD COLUMN normalization_version INTEGER DEFAULT 1;



CREATE INDEX idx_message_signals_normalized
    ON message_signals(normalized);



-- =====================================================
-- 2️⃣ USER_ENTITIES improvements
-- =====================================================

ALTER TABLE user_entities
    ADD COLUMN canonical_name TEXT;

ALTER TABLE user_entities
    ADD COLUMN confidence DOUBLE PRECISION;

ALTER TABLE user_entities
    ADD COLUMN source_signal_id UUID;

ALTER TABLE user_entities
    ADD COLUMN extraction_model VARCHAR(100);

ALTER TABLE user_entities
    ADD COLUMN pipeline_version VARCHAR(50);


ALTER TABLE user_entities
    ADD CONSTRAINT fk_user_entities_signal
        FOREIGN KEY (source_signal_id)
            REFERENCES message_signals(id)
            ON DELETE SET NULL;



-- =====================================================
-- 3️⃣ USER_ACTIVITIES traceability
-- =====================================================

ALTER TABLE user_activities
    ADD COLUMN signal_id UUID;

ALTER TABLE user_activities
    ADD COLUMN source_hash TEXT;

ALTER TABLE user_activities
    ADD COLUMN extraction_model VARCHAR(100);

ALTER TABLE user_activities
    ADD COLUMN pipeline_version VARCHAR(50);


ALTER TABLE user_activities
    ADD CONSTRAINT fk_activity_signal
        FOREIGN KEY (signal_id)
            REFERENCES message_signals(id)
            ON DELETE SET NULL;



CREATE INDEX idx_user_activities_signal
    ON user_activities(signal_id);



-- =====================================================
-- 4️⃣ USER_TOPICS traceability
-- =====================================================

ALTER TABLE user_topics
    ADD COLUMN signal_id UUID;

ALTER TABLE user_topics
    ADD COLUMN source_hash TEXT;

ALTER TABLE user_topics
    ADD COLUMN extraction_model VARCHAR(100);

ALTER TABLE user_topics
    ADD COLUMN pipeline_version VARCHAR(50);


ALTER TABLE user_topics
    ADD CONSTRAINT fk_topic_signal
        FOREIGN KEY (signal_id)
            REFERENCES message_signals(id)
            ON DELETE SET NULL;



-- =====================================================
-- 5️⃣ INTENT_ITEMS traceability
-- =====================================================

ALTER TABLE intent_items
    ADD COLUMN signal_id UUID;

ALTER TABLE intent_items
    ADD COLUMN source_hash TEXT;

ALTER TABLE intent_items
    ADD COLUMN extraction_model VARCHAR(100);

ALTER TABLE intent_items
    ADD COLUMN pipeline_version VARCHAR(50);


ALTER TABLE intent_items
    ADD CONSTRAINT fk_intent_signal
        FOREIGN KEY (signal_id)
            REFERENCES message_signals(id)
            ON DELETE SET NULL;



-- =====================================================
-- 6️⃣ USER_PREFERENCES traceability
-- =====================================================

ALTER TABLE user_preferences
    ADD COLUMN signal_id UUID;

ALTER TABLE user_preferences
    ADD COLUMN extraction_model VARCHAR(100);

ALTER TABLE user_preferences
    ADD COLUMN pipeline_version VARCHAR(50);


ALTER TABLE user_preferences
    ADD CONSTRAINT fk_pref_signal
        FOREIGN KEY (signal_id)
            REFERENCES message_signals(id)
            ON DELETE SET NULL;



-- =====================================================
-- 7️⃣ COGNITIVE_STATES traceability
-- =====================================================

ALTER TABLE cognitive_states
    ADD COLUMN signal_id UUID;

ALTER TABLE cognitive_states
    ADD COLUMN extraction_model VARCHAR(100);

ALTER TABLE cognitive_states
    ADD COLUMN pipeline_version VARCHAR(50);


ALTER TABLE cognitive_states
    ADD CONSTRAINT fk_cognitive_signal
        FOREIGN KEY (signal_id)
            REFERENCES message_signals(id)
            ON DELETE SET NULL;



-- =====================================================
-- 8️⃣ DAILY_BEHAVIOR_METRICS enhancements
-- =====================================================

ALTER TABLE daily_behavior_metrics
    ADD COLUMN signal_count INTEGER DEFAULT 0;

ALTER TABLE daily_behavior_metrics
    ADD COLUMN last_signal_at TIMESTAMPTZ;