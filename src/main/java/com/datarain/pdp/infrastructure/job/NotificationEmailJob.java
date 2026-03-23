package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "jobs.notification-email.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationEmailJob extends AbstractMonitoredJob {

    private final NotificationService notificationService;

    public NotificationEmailJob(NotificationService notificationService,
                                JobMonitoringService jobMonitoringService) {
        super(jobMonitoringService);
        this.notificationService = notificationService;
    }

    @Transactional
//    @Scheduled(cron = "0 */5 * * * ?")
    @Scheduled(cron = "0 0 * * * ?") // هر ساعت
    public void processPendingNotifications() {
        long processed = executeMonitored("NotificationEmailJob", () -> {
            long updated = notificationService.markPendingAsSent();
            log.info("NotificationEmailJob finished: {} notifications marked as SENT", updated);
            return updated;
        });
        log.debug("NotificationEmailJob processed count: {}", processed);
    }
}
