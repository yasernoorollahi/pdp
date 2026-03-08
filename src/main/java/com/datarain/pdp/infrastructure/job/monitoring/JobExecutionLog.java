package com.datarain.pdp.infrastructure.job.monitoring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "job_execution_log",
        indexes = {
                @Index(name = "idx_job_execution_log_job_name", columnList = "job_name"),
                @Index(name = "idx_job_execution_log_started_at", columnList = "started_at"),
                @Index(name = "idx_job_execution_log_status", columnList = "status")
        })
public class JobExecutionLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "job_name", nullable = false, length = 150)
    private String jobName;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobExecutionStatus status;

    @Column(nullable = false)
    private long duration;

    @Column(name = "processed_count", nullable = false)
    private long processedCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @PrePersist
    protected void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
