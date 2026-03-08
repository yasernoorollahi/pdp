package com.datarain.pdp.infrastructure.job.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobMonitoringService {

    private final JobExecutionLogRepository jobExecutionLogRepository;
    private final MeterRegistry meterRegistry;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long execute(String jobName, MonitoredJobAction action) {
        Instant startedAt = Instant.now();
        try {
            long processedCount = action.run();
            Instant finishedAt = Instant.now();
            long durationMs = Duration.between(startedAt, finishedAt).toMillis();

            saveExecution(jobName, startedAt, finishedAt, JobExecutionStatus.SUCCESS, durationMs, processedCount, null);
            recordSuccessMetrics(jobName, durationMs);
            return processedCount;
        } catch (Exception ex) {
            Instant finishedAt = Instant.now();
            long durationMs = Duration.between(startedAt, finishedAt).toMillis();

            saveExecution(jobName, startedAt, finishedAt, JobExecutionStatus.FAILED, durationMs, 0, sanitize(ex.getMessage()));
            recordFailureMetrics(jobName, durationMs);
            log.error("Job execution failed: {}", jobName, ex);
            throw new IllegalStateException("Job failed: " + jobName, ex);
        }
    }

    private void saveExecution(String jobName,
                               Instant startedAt,
                               Instant finishedAt,
                               JobExecutionStatus status,
                               long durationMs,
                               long processedCount,
                               String errorMessage) {
        JobExecutionLog logEntry = new JobExecutionLog();
        logEntry.setJobName(jobName);
        logEntry.setStartedAt(startedAt);
        logEntry.setFinishedAt(finishedAt);
        logEntry.setStatus(status);
        logEntry.setDuration(durationMs);
        logEntry.setProcessedCount(processedCount);
        logEntry.setErrorMessage(errorMessage);
        jobExecutionLogRepository.save(logEntry);
    }

    private void recordSuccessMetrics(String jobName, long durationMs) {
        meterRegistry.counter("pdp.jobs.execution.success", "job", jobName).increment();
        meterRegistry.timer("pdp.jobs.execution.duration", "job", jobName, "status", "success")
                .record(Duration.ofMillis(durationMs));
    }

    private void recordFailureMetrics(String jobName, long durationMs) {
        meterRegistry.counter("pdp.jobs.execution.failure", "job", jobName).increment();
        meterRegistry.timer("pdp.jobs.execution.duration", "job", jobName, "status", "failure")
                .record(Duration.ofMillis(durationMs));
    }

    private String sanitize(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
