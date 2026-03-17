package com.datarain.pdp.admin.service.impl;

import com.datarain.pdp.admin.dto.AdminSystemOverviewRequest;
import com.datarain.pdp.admin.dto.AdminSystemOverviewResponse;
import com.datarain.pdp.admin.dto.BusinessStatsResponse;
import com.datarain.pdp.admin.dto.JobExecutionLogResponse;
import com.datarain.pdp.admin.dto.SystemOverviewResponse;
import com.datarain.pdp.admin.mapper.AdminMonitoringMapper;
import com.datarain.pdp.admin.model.AdminBusinessStats;
import com.datarain.pdp.admin.repository.AdminMonitoringRepository;
import com.datarain.pdp.admin.service.AdminMonitoringService;
import com.datarain.pdp.admin.service.SystemOverviewService;
import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminMonitoringServiceImpl implements AdminMonitoringService {

    private static final int DEFAULT_JOBS_LIMIT = 20;

    private final AdminMonitoringRepository adminMonitoringRepository;
    private final SystemOverviewService systemOverviewService;
    private final SecurityAuditService securityAuditService;
    private final PdpMetrics metrics;

    @Override
    public AdminSystemOverviewResponse getSystemOverview(AdminSystemOverviewRequest request, HttpServletRequest httpRequest) {
        int jobsLimit = request.resolveJobsLimit(DEFAULT_JOBS_LIMIT);
        UUID userId = SecurityUtils.currentUserId();
        String email = SecurityUtils.currentUsername();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "admin.system_overview.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("jobsLimit", jobsLimit)
                .addKeyValue("traceId", traceId)
                .log("Admin system overview requested");

        metrics.getAdminSystemOverviewCounter().increment();
        securityAuditService.log(SecurityEventType.ADMIN_SYSTEM_OVERVIEW_VIEWED, email, userId,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"),
                "jobsLimit=" + jobsLimit, true);

        BusinessStatsResponse businessStats = getBusinessStatsInternal();
        SystemOverviewResponse overview = systemOverviewService.getOverview();
        List<JobExecutionLogResponse> recentJobs = getRecentJobsInternal(PageRequest.of(0, jobsLimit)).getContent();

        return new AdminSystemOverviewResponse(
                businessStats,
                overview,
                recentJobs,
                Instant.now()
        );
    }

    private BusinessStatsResponse getBusinessStatsInternal() {
        AdminBusinessStats stats = adminMonitoringRepository.loadBusinessStats(Instant.now());
        return AdminMonitoringMapper.toBusinessStatsResponse(stats);
    }

    private Page<JobExecutionLogResponse> getRecentJobsInternal(Pageable pageable) {
        return adminMonitoringRepository.findRecentJobs(pageable)
                .map(AdminMonitoringMapper::toJobExecutionLogResponse);
    }
}
