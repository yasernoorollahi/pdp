package com.datarain.pdp.infrastructure.job.monitoring;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, UUID> {
    Page<JobExecutionLog> findAllByOrderByStartedAtDesc(Pageable pageable);
}
