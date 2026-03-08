CREATE TABLE job_execution_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_name VARCHAR(150) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    duration BIGINT NOT NULL,
    processed_count BIGINT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000)
);

CREATE INDEX idx_job_execution_log_job_name ON job_execution_log(job_name);
CREATE INDEX idx_job_execution_log_started_at ON job_execution_log(started_at DESC);
CREATE INDEX idx_job_execution_log_status ON job_execution_log(status);
