-- AI signal engine hardening

ALTER TABLE user_messages
    ADD CONSTRAINT chk_user_messages_processing_status
        CHECK (processing_status IN ('PENDING', 'PROCESSING', 'DONE', 'FAILED'));

CREATE INDEX idx_user_messages_signal_decision_processing_status
    ON user_messages (signal_decision, processing_status, created_at);
