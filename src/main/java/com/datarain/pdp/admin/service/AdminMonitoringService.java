package com.datarain.pdp.admin.service;

import com.datarain.pdp.admin.dto.AdminSystemOverviewResponse;
import com.datarain.pdp.admin.dto.BusinessStatsResponse;
import com.datarain.pdp.admin.dto.JobExecutionLogResponse;
import com.datarain.pdp.admin.dto.SystemOverviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminMonitoringService {
    AdminSystemOverviewResponse getSystemOverview(int jobsLimit);
    Page<JobExecutionLogResponse> getRecentJobs(Pageable pageable);
    BusinessStatsResponse getBusinessStats();
    SystemOverviewResponse getOverview();
}
