CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    version BIGINT,
    tenant_id UUID,
    user_id UUID,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
