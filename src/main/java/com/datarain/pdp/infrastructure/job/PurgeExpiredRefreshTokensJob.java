package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.auth.repository.RefreshTokenRepository;
import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import lombok.extern.slf4j.Slf4j;
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

    public PurgeExpiredRefreshTokensJob(RefreshTokenRepository repository, JobMonitoringService jobMonitoringService) {
        super(jobMonitoringService);
        this.repository = repository;
    }


    @Transactional
    @Scheduled(cron = "0 */1 * * * ?") //every 1 min
//    @Scheduled(cron = "0 0 * * * ?") // هر ساعت
    public void purge() {
        long count = executeMonitored("PurgeExpiredRefreshTokensJob", () -> {
            log.info("PurgeExpiredRefreshTokensJob started");
            long removed = repository.deleteByExpiryDateBefore(Instant.now());
            log.info("PurgeExpiredRefreshTokensJob finished: {} tokens removed", removed);
            return removed;
        });
        log.debug("PurgeExpiredRefreshTokensJob processed count: {}", count);
    }
}
