package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.notification.entity.NotificationEntity;
import com.datarain.pdp.notification.entity.NotificationStatus;
import com.datarain.pdp.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "jobs.notification-email.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationEmailJob extends AbstractMonitoredJob {

    private final NotificationRepository notificationRepository;

    public NotificationEmailJob(NotificationRepository notificationRepository,
                                JobMonitoringService jobMonitoringService) {
        super(jobMonitoringService);
        this.notificationRepository = notificationRepository;
    }

    @Transactional
//    @Scheduled(cron = "0 */5 * * * ?")
    @Scheduled(cron = "0 0 * * * ?") // هر ساعت
    public void processPendingNotifications() {
        long processed = executeMonitored("NotificationEmailJob", () -> {
            List<NotificationEntity> pending = notificationRepository
                    .findTop100ByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING);

            pending.forEach(notification -> notification.setStatus(NotificationStatus.SENT));
            notificationRepository.saveAll(pending);
            log.info("NotificationEmailJob finished: {} notifications marked as SENT", pending.size());
            return pending.size();
        });
        log.debug("NotificationEmailJob processed count: {}", processed);
    }
}
