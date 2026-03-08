package com.datarain.pdp.admin.controller;

import com.datarain.pdp.admin.dto.AdminSystemOverviewResponse;
import com.datarain.pdp.admin.dto.BusinessStatsResponse;
import com.datarain.pdp.admin.dto.JobExecutionLogResponse;
import com.datarain.pdp.admin.dto.SystemOverviewResponse;
import com.datarain.pdp.admin.service.AdminMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminMonitoringController {

    private final AdminMonitoringService adminMonitoringService;

    @GetMapping("/stats")
    public BusinessStatsResponse getStats() {
        return adminMonitoringService.getBusinessStats();
    }

    @GetMapping("/overview")
    public SystemOverviewResponse getOverview() {
        return adminMonitoringService.getOverview();
    }

    @GetMapping("/system-overview")
    public AdminSystemOverviewResponse getSystemOverview(
            @RequestParam(name = "jobsLimit", defaultValue = "20") int jobsLimit
    ) {
        return adminMonitoringService.getSystemOverview(jobsLimit);
    }

    @GetMapping("/jobs")
    public Page<JobExecutionLogResponse> getRecentJobs(
            @ParameterObject
            @PageableDefault(size = 50, sort = "startedAt")
            Pageable pageable
    ) {
        return adminMonitoringService.getRecentJobs(pageable);
    }
}
