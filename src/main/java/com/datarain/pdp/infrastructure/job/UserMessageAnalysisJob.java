package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.external.ai.AiExtractionProperties;
import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.message.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "jobs.user-message-analysis.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class UserMessageAnalysisJob extends AbstractMonitoredJob {

    private final UserMessageService userMessageService;
    private final AiExtractionProperties aiExtractionProperties;
    private final int batchSize;

    public UserMessageAnalysisJob(UserMessageService userMessageService,
                                  AiExtractionProperties aiExtractionProperties,
                                  JobMonitoringService jobMonitoringService,
                                  @Value("${jobs.user-message-analysis.batch-size:50}") int batchSize) {
        super(jobMonitoringService);
        this.userMessageService = userMessageService;
        this.aiExtractionProperties = aiExtractionProperties;
        this.batchSize = batchSize;
    }

    @Transactional
    @Scheduled(cron = "${jobs.user-message-analysis.cron:0 */2 * * * ?}")
    @SchedulerLock(name = "UserMessageAnalysisJob", lockAtMostFor = "PT5M")
    public void analyzePendingMessages() {
        long processed = executeMonitored("UserMessageAnalysisJob", () -> {
            String provider = aiExtractionProperties.getDefaultProvider();
            String model = aiExtractionProperties.getDefaultModel();
            log.atInfo()
                    .addKeyValue("event", "user.message.analysis.job.started")
                    .addKeyValue("batchSize", batchSize)
                    .addKeyValue("provider", provider)
                    .addKeyValue("model", model)
                    .log("User message analysis job started");

            long count = userMessageService.analyzePendingMessages(batchSize, provider, model);

            log.atInfo()
                    .addKeyValue("event", "user.message.analysis.job.finished")
                    .addKeyValue("processedCount", count)
                    .log("User message analysis job finished");
            return count;
        });
        log.debug("UserMessageAnalysisJob processed count: {}", processed);
    }
}
