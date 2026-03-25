package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.external.ai.AiExtractionProperties;
import com.datarain.pdp.infrastructure.job.control.JobControlResolver;
import com.datarain.pdp.infrastructure.job.control.ManagedJob;
import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.signal.service.AiSignalEngineService;
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
        name = "jobs.ai-signal-engine.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AiSignalEngineJob extends AbstractMonitoredJob {

    private final AiSignalEngineService aiSignalEngineService;
    private final AiExtractionProperties aiExtractionProperties;
    private final int batchSize;
    private final int maxRetries;
    private final JobControlResolver jobControlResolver;

    public AiSignalEngineJob(AiSignalEngineService aiSignalEngineService,
                             AiExtractionProperties aiExtractionProperties,
                             JobMonitoringService jobMonitoringService,
                             @Value("${jobs.ai-signal-engine.batch-size:20}") int batchSize,
                             @Value("${jobs.ai-signal-engine.max-retries:3}") int maxRetries,
                             JobControlResolver jobControlResolver) {
        super(jobMonitoringService);
        this.aiSignalEngineService = aiSignalEngineService;
        this.aiExtractionProperties = aiExtractionProperties;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.jobControlResolver = jobControlResolver;
    }

    @Transactional
    @Scheduled(cron = "${jobs.ai-signal-engine.cron:0 */3 * * * ?}")
    @SchedulerLock(name = "AiSignalEngineJob", lockAtMostFor = "PT5M")
    public void run() {
        if (!jobControlResolver.isJobEnabled(ManagedJob.AI_SIGNAL_ENGINE)) {
            log.info("AiSignalEngineJob skipped (disabled by admin control).");
            return;
        }
        long processed = executeMonitored("AiSignalEngineJob", () -> {
            String provider = aiExtractionProperties.getDefaultProvider();
            String model = aiExtractionProperties.getDefaultModel();

            log.atInfo()
                    .addKeyValue("event", "ai.signal.engine.job.started")
                    .addKeyValue("batchSize", batchSize)
                    .addKeyValue("maxRetries", maxRetries)
                    .addKeyValue("provider", provider)
                    .addKeyValue("model", model)
                    .log("AI signal engine job started");

            long count = aiSignalEngineService.processPendingUsefulMessages(batchSize, maxRetries, provider, model);

            log.atInfo()
                    .addKeyValue("event", "ai.signal.engine.job.finished")
                    .addKeyValue("processedCount", count)
                    .log("AI signal engine job finished");
            return count;
        });

        log.debug("AiSignalEngineJob processed count: {}", processed);
    }
}
