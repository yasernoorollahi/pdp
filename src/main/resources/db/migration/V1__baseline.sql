CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       version BIGINT,
                       tenant_id UUID,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       created_by UUID,
                       updated_by UUID,
                       failed_login_attempts INTEGER NOT NULL DEFAULT 0,
                       locked_until TIMESTAMPTZ,
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       email_verification_token VARCHAR(255),
                       password_reset_token VARCHAR(255),
                       password_reset_token_expiry TIMESTAMPTZ
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            CONSTRAINT fk_user_role FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                version BIGINT,
                                tenant_id UUID,
                                token TEXT NOT NULL UNIQUE,
                                expiry_date TIMESTAMPTZ NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                device VARCHAR(255) NOT NULL,
                                ip_address VARCHAR(50) NOT NULL,
                                revoked_at TIMESTAMPTZ,
                                user_id UUID NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                created_by UUID,
                                updated_by UUID,
                                enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                CONSTRAINT fk_refresh_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);

CREATE TABLE security_audit_logs (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id UUID,
                                     email VARCHAR(255) NOT NULL,
                                     event_type VARCHAR(50) NOT NULL,
                                     ip_address VARCHAR(50),
                                     user_agent VARCHAR(500),
                                     details TEXT,
                                     success BOOLEAN NOT NULL DEFAULT TRUE,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_locked_until ON users(locked_until)
    WHERE locked_until IS NOT NULL;
CREATE INDEX idx_users_email_verification_token ON users(email_verification_token)
    WHERE email_verification_token IS NOT NULL;

CREATE INDEX idx_audit_user_id ON security_audit_logs(user_id);
CREATE INDEX idx_audit_event_type ON security_audit_logs(event_type);
CREATE INDEX idx_audit_created_at ON security_audit_logs(created_at DESC);
CREATE INDEX idx_audit_email ON security_audit_logs(email);

INSERT INTO users (id, email, password_hash, enabled)
VALUES (
           gen_random_uuid(),
           'admin@pdp.local',
           '$2b$12$4LwGAKzyTRQN5ecRV6WWV.B5fwgbgKbDVm0zXAA5QLwJRV3k.RcjG',
           true
       );

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE email = 'admin@pdp.local';
