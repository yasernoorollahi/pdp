-- =====================================================
-- V8__add_ai_pipeline_fields.sql
-- AI Processing Pipeline Enhancements
-- =====================================================


-- =====================================================
-- 1️⃣ USER_MESSAGES enhancements
-- =====================================================

ALTER TABLE user_messages
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE user_messages
    ADD COLUMN processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE user_messages
    ADD COLUMN processing_started_at TIMESTAMPTZ;



-- Index for pipeline job
CREATE INDEX idx_user_messages_processing_status
    ON user_messages(processing_status);



-- =====================================================
-- 2️⃣ MESSAGE_SIGNALS enhancements
-- =====================================================

ALTER TABLE message_signals
    ADD COLUMN extractor_model VARCHAR(100);

ALTER TABLE message_signals
    ADD COLUMN extraction_latency_ms INTEGER;

ALTER TABLE message_signals
    ADD COLUMN pipeline_version VARCHAR(50);