package com.datarain.pdp.admin.repository;

import com.datarain.pdp.admin.dto.AdminBusinessStats;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AdminMonitoringRepository {
    AdminBusinessStats loadBusinessStats(Instant now);

    Page<JobExecutionLog> findRecentJobs(Pageable pageable);
}
