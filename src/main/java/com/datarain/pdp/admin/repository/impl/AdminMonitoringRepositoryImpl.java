package com.datarain.pdp.admin.repository.impl;

import com.datarain.pdp.admin.model.AdminBusinessStats;
import com.datarain.pdp.admin.repository.AdminMonitoringRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class AdminMonitoringRepositoryImpl implements AdminMonitoringRepository {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationRepository notificationRepository;
    private final JobExecutionLogRepository jobExecutionLogRepository;

    @Override
    public AdminBusinessStats loadBusinessStats(Instant now) {
        return new AdminBusinessStats(
                userRepository.count(),
                userRepository.countByEnabled(true),
                userRepository.countByLockedUntilAfter(now),
                itemRepository.count(),
                itemRepository.countByStatus(ItemStatus.ACTIVE),
                itemRepository.countByStatus(ItemStatus.ARCHIVED),
                refreshTokenRepository.count(),
                refreshTokenRepository.countByRevokedFalseAndExpiryDateAfter(now),
                notificationRepository.countByStatus(NotificationStatus.PENDING)
        );
    }

    @Override
    public Page<JobExecutionLog> findRecentJobs(Pageable pageable) {
        return jobExecutionLogRepository.findAllByOrderByStartedAtDesc(pageable);
    }
}
