CREATE TABLE IF NOT EXISTS job_control_settings (
    job_key VARCHAR(100) PRIMARY KEY,
    enabled_override BOOLEAN,
    updated_at TIMESTAMP(3) NOT NULL,
    updated_by UUID
);
