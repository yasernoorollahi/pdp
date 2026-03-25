package com.datarain.pdp.admin.service.impl;

import com.datarain.pdp.admin.dto.JobControlChangeResponse;
import com.datarain.pdp.admin.dto.JobControlResponse;
import com.datarain.pdp.admin.dto.JobControlUpdateRequest;
import com.datarain.pdp.admin.dto.JobStatusResponse;
import com.datarain.pdp.admin.dto.JobToggleRequest;
import com.datarain.pdp.admin.service.AdminJobControlService;
import com.datarain.pdp.exception.business.InvalidJobKeyException;
import com.datarain.pdp.infrastructure.audit.BusinessEventService;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.infrastructure.job.control.JobControlResolver;
import com.datarain.pdp.infrastructure.job.control.JobControlSetting;
import com.datarain.pdp.infrastructure.job.control.JobControlSettingRepository;
import com.datarain.pdp.infrastructure.job.control.ManagedJob;
import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminJobControlServiceImpl implements AdminJobControlService {

    private final JobControlResolver resolver;
    private final JobControlSettingRepository repository;
    private final BusinessEventService businessEventService;
    private final SecurityAuditService securityAuditService;
    private final PdpMetrics metrics;

    @Override
    @Transactional(readOnly = true)
    public JobControlResponse getStatus() {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        log.atInfo()
                .addKeyValue("event", "admin.job_control.viewed")
                .addKeyValue("traceId", traceId)
                .log("Admin job control status requested");
        metrics.getAdminJobControlViewedCounter().increment();
        return buildResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public JobControlChangeResponse getLatestChange() {
        return repository.findTopByOrderByUpdatedAtDesc()
                .map(setting -> new JobControlChangeResponse(
                        setting.getJobKey(),
                        setting.getEnabledOverride(),
                        setting.getUpdatedAt(),
                        setting.getUpdatedBy()
                ))
                .orElse(null);
    }

    @Override
    public JobControlResponse update(JobControlUpdateRequest request, HttpServletRequest httpRequest) {
        UUID userId = SecurityUtils.currentUserId();
        String email = SecurityUtils.currentUsername();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        Instant updateTime = resolveMonotonicUpdateTime();

        log.atInfo()
                .addKeyValue("event", "admin.job_control.updated")
                .addKeyValue("userId", userId)
                .addKeyValue("traceId", traceId)
                .log("Admin job control update requested");

        if (request.globalEnabled() != null) {
            upsert(JobControlResolver.GLOBAL_KEY, request.globalEnabled(), userId, updateTime);
        }

        List<JobToggleRequest> jobs = request.jobs();
        if (jobs != null) {
            for (JobToggleRequest toggle : jobs) {
                ManagedJob job = ManagedJob.fromKey(toggle.jobKey());
                if (job == null) {
                    throw new InvalidJobKeyException(toggle.jobKey());
                }
                upsert(job.getKey(), toggle.enabled(), userId, updateTime);
            }
        }

        metrics.getAdminJobControlUpdatedCounter().increment();
        businessEventService.log(
                BusinessEventType.ADMIN_JOB_CONTROL_UPDATED,
                email,
                userId,
                summarizeUpdate(request),
                true
        );
        securityAuditService.log(
                SecurityEventType.ADMIN_JOB_CONTROL_UPDATED,
                email,
                userId,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"),
                summarizeUpdate(request),
                true
        );

        return buildResponse();
    }

    private JobControlResponse buildResponse() {
        var states = resolver.resolveAll();
        List<JobStatusResponse> jobs = new ArrayList<>();

        for (ManagedJob job : ManagedJob.values()) {
            var state = states.get(job);
            jobs.add(new JobStatusResponse(
                    job.getKey(),
                    job.getDescription(),
                    state.configuredEnabled(),
                    state.overrideEnabled(),
                    state.effectiveEnabled()
            ));
        }

        jobs.sort(Comparator.comparing(JobStatusResponse::jobKey));

        return new JobControlResponse(
                resolver.isGlobalEnabled(),
                resolver.globalOverride(),
                jobs,
                Instant.now()
        );
    }

    private void upsert(String key, boolean enabled, UUID userId, Instant updateTime) {
        JobControlSetting setting = repository.findById(key).orElseGet(JobControlSetting::new);
        setting.setJobKey(key);
        setting.setEnabledOverride(enabled);
        setting.setUpdatedAt(updateTime);
        setting.setUpdatedBy(userId);
        repository.save(setting);
    }

    private Instant resolveMonotonicUpdateTime() {
        Instant now = Instant.now();
        return repository.findTopByOrderByUpdatedAtDesc()
                .map(JobControlSetting::getUpdatedAt)
                .filter(latest -> latest.isAfter(now))
                .map(latest -> latest.plusMillis(1))
                .orElse(now);
    }

    private String summarizeUpdate(JobControlUpdateRequest request) {
        StringBuilder details = new StringBuilder();
        if (request.globalEnabled() != null) {
            details.append("global=").append(request.globalEnabled());
        }
        if (request.jobs() != null && !request.jobs().isEmpty()) {
            if (!details.isEmpty()) {
                details.append("; ");
            }
            details.append("jobs=");
            details.append(request.jobs().stream()
                    .map(j -> j.jobKey() + ":" + j.enabled())
                    .reduce((a, b) -> a + "," + b)
                    .orElse(""));
        }
        return details.toString();
    }
}
