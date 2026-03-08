-- Add AI analysis fields to user_messages table

ALTER TABLE user_messages
    ADD COLUMN analysis_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL;

ALTER TABLE user_messages
    ADD COLUMN signal_score DOUBLE PRECISION;

ALTER TABLE user_messages
    ADD COLUMN signal_decision VARCHAR(20);

ALTER TABLE user_messages
    ADD COLUMN signal_reason TEXT;

ALTER TABLE user_messages
    ADD COLUMN processed_at TIMESTAMP;

-- Optional index for background job performance
CREATE INDEX idx_user_messages_analysis_status
    ON user_messages (analysis_status);