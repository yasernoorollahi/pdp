package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.infrastructure.job.control.JobControlResolver;
import com.datarain.pdp.infrastructure.job.control.ManagedJob;
import com.datarain.pdp.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
    private final JobControlResolver jobControlResolver;

    public NotificationEmailJob(NotificationService notificationService,
                                JobMonitoringService jobMonitoringService,
                                JobControlResolver jobControlResolver) {
        super(jobMonitoringService);
        this.notificationService = notificationService;
        this.jobControlResolver = jobControlResolver;
    }

    @Transactional
//    @Scheduled(cron = "0 */5 * * * ?")
    @Scheduled(cron = "0 0 * * * ?") // هر ساعت
    @SchedulerLock(name = "NotificationEmailJob", lockAtMostFor = "PT30M")
    public void processPendingNotifications() {
        if (!jobControlResolver.isJobEnabled(ManagedJob.NOTIFICATION_EMAIL)) {
            log.info("NotificationEmailJob skipped (disabled by admin control).");
            return;
        }
        long processed = executeMonitored("NotificationEmailJob", () -> {
            long updated = notificationService.markPendingAsSent();
            log.info("NotificationEmailJob finished: {} notifications marked as SENT", updated);
            return updated;
        });
        log.debug("NotificationEmailJob processed count: {}", processed);
    }
}
