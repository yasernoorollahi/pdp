CREATE TABLE moderation_cases (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID,

    target_type VARCHAR(20) NOT NULL,
    target_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    risk_score INTEGER NOT NULL,
    ai_confidence DOUBLE PRECISION,
    reason_category VARCHAR(50) NOT NULL,
    source VARCHAR(20) NOT NULL,

    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    comment VARCHAR(1000),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_moderation_risk_score_range CHECK (risk_score >= 0 AND risk_score <= 100),
    CONSTRAINT chk_moderation_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'AUTO_BLOCKED')),
    CONSTRAINT chk_moderation_target_type CHECK (target_type IN ('USER', 'CONTENT')),
    CONSTRAINT chk_moderation_source CHECK (source IN ('MANUAL', 'AI', 'SYSTEM'))
);

CREATE INDEX idx_moderation_cases_status ON moderation_cases(status);
CREATE INDEX idx_moderation_cases_target_type_target_id ON moderation_cases(target_type, target_id);
CREATE INDEX idx_moderation_cases_created_at ON moderation_cases(created_at DESC);
