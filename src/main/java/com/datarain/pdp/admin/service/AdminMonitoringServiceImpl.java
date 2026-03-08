package com.datarain.pdp.admin.service;

import com.datarain.pdp.admin.dto.AdminSystemOverviewResponse;
import com.datarain.pdp.admin.dto.BusinessStatsResponse;
import com.datarain.pdp.admin.dto.JobExecutionLogResponse;
import com.datarain.pdp.admin.dto.SystemOverviewResponse;
import com.datarain.pdp.auth.repository.RefreshTokenRepository;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionLog;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionLogRepository;
import com.datarain.pdp.item.entity.ItemStatus;
import com.datarain.pdp.item.repository.ItemRepository;
import com.datarain.pdp.notification.entity.NotificationStatus;
import com.datarain.pdp.notification.repository.NotificationRepository;
import com.datarain.pdp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminMonitoringServiceImpl implements AdminMonitoringService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationRepository notificationRepository;
    private final JobExecutionLogRepository jobExecutionLogRepository;
    private final SystemOverviewService systemOverviewService;

    @Override
    public AdminSystemOverviewResponse getSystemOverview(int jobsLimit) {
        return new AdminSystemOverviewResponse(
                getBusinessStats(),
                systemOverviewService.getOverview(),
                getRecentJobs(PageRequest.of(0, Math.max(1, Math.min(jobsLimit, 200)))).getContent(),
                Instant.now()
        );
    }

    @Override
    public Page<JobExecutionLogResponse> getRecentJobs(Pageable pageable) {
        return jobExecutionLogRepository.findAllByOrderByStartedAtDesc(pageable)
                .map(this::toJobExecutionResponse);
    }

    @Override
    public BusinessStatsResponse getBusinessStats() {
        return new BusinessStatsResponse(
                userRepository.count(),
                userRepository.countByEnabled(true),
                userRepository.countByLockedUntilAfter(Instant.now()),
                itemRepository.count(),
                itemRepository.countByStatus(ItemStatus.ACTIVE),
                itemRepository.countByStatus(ItemStatus.ARCHIVED),
                refreshTokenRepository.count(),
                refreshTokenRepository.countByRevokedFalseAndExpiryDateAfter(Instant.now()),
                notificationRepository.countByStatus(NotificationStatus.PENDING)
        );
    }

    @Override
    public SystemOverviewResponse getOverview() {
        return systemOverviewService.getOverview();
    }

    private JobExecutionLogResponse toJobExecutionResponse(JobExecutionLog entity) {
        return new JobExecutionLogResponse(
                entity.getId(),
                entity.getJobName(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getStatus(),
                entity.getDuration(),
                entity.getProcessedCount(),
                entity.getErrorMessage()
        );
    }
}
