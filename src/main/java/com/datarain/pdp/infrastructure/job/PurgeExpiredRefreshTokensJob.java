package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.auth.repository.RefreshTokenRepository;
import com.datarain.pdp.infrastructure.job.control.JobControlResolver;
import com.datarain.pdp.infrastructure.job.control.ManagedJob;
import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "jobs.refresh-token.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PurgeExpiredRefreshTokensJob extends AbstractMonitoredJob {

    private final RefreshTokenRepository repository;
    private final JobControlResolver jobControlResolver;

    public PurgeExpiredRefreshTokensJob(RefreshTokenRepository repository,
                                        JobMonitoringService jobMonitoringService,
                                        JobControlResolver jobControlResolver) {
        super(jobMonitoringService);
        this.repository = repository;
        this.jobControlResolver = jobControlResolver;
    }


    @Transactional
    @Scheduled(cron = "0 */1 * * * ?") //every 1 min
    @SchedulerLock(name = "PurgeExpiredRefreshTokensJob", lockAtMostFor = "PT5M")
//    @Scheduled(cron = "0 0 * * * ?") // هر ساعت
    public void purge() {
        if (!jobControlResolver.isJobEnabled(ManagedJob.PURGE_EXPIRED_REFRESH_TOKENS)) {
            log.info("PurgeExpiredRefreshTokensJob skipped (disabled by admin control).");
            return;
        }
        long count = executeMonitored("PurgeExpiredRefreshTokensJob", () -> {
            log.info("PurgeExpiredRefreshTokensJob started");
            long removed = repository.deleteByExpiryDateBefore(Instant.now());
            log.info("PurgeExpiredRefreshTokensJob finished: {} tokens removed", removed);
            return removed;
        });
        log.debug("PurgeExpiredRefreshTokensJob processed count: {}", count);
    }
}
