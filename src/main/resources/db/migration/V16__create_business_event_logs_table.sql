CREATE TABLE business_event_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    email VARCHAR(255),
    event_type VARCHAR(50) NOT NULL,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    success BOOLEAN NOT NULL
);

CREATE INDEX idx_business_event_user_id ON business_event_logs (user_id);
CREATE INDEX idx_business_event_type ON business_event_logs (event_type);
CREATE INDEX idx_business_event_created_at ON business_event_logs (created_at);
CREATE INDEX idx_business_event_email ON business_event_logs (email);

INSERT INTO business_event_logs (id, user_id, email, event_type, details, created_at, success)
SELECT id, user_id, email, event_type, details, created_at, success
FROM security_audit_logs
WHERE event_type IN (
    'EXTRACTION_REQUESTED',
    'MODERATION_CASE_CREATED',
    'MODERATION_CASE_APPROVED',
    'MODERATION_CASE_REJECTED',
    'MODERATION_CASE_AUTO_BLOCKED',
    'USER_MESSAGE_CREATED',
    'USER_MESSAGE_UPDATED',
    'USER_MESSAGE_DELETED',
    'USER_MESSAGE_PROCESSED',
    'SIGNAL_ENGINE_EXECUTED',
    'SIGNAL_NORMALIZATION_EXECUTED',
    'INSIGHTS_VIEWED',
    'ADMIN_SYSTEM_OVERVIEW_VIEWED',
    'TEST_DATA_SEEDED'
);

DELETE FROM security_audit_logs
WHERE event_type IN (
    'EXTRACTION_REQUESTED',
    'MODERATION_CASE_CREATED',
    'MODERATION_CASE_APPROVED',
    'MODERATION_CASE_REJECTED',
    'MODERATION_CASE_AUTO_BLOCKED',
    'USER_MESSAGE_CREATED',
    'USER_MESSAGE_UPDATED',
    'USER_MESSAGE_DELETED',
    'USER_MESSAGE_PROCESSED',
    'SIGNAL_ENGINE_EXECUTED',
    'SIGNAL_NORMALIZATION_EXECUTED',
    'INSIGHTS_VIEWED',
    'ADMIN_SYSTEM_OVERVIEW_VIEWED',
    'TEST_DATA_SEEDED'
);
