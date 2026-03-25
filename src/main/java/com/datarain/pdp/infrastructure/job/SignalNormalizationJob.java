package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import com.datarain.pdp.infrastructure.job.control.JobControlResolver;
import com.datarain.pdp.infrastructure.job.control.ManagedJob;
import com.datarain.pdp.signal.normalization.service.SignalNormalizationService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "jobs.signal-normalization.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SignalNormalizationJob extends AbstractMonitoredJob {

    private final SignalNormalizationService signalNormalizationService;
    private final int batchSize;
    private final JobControlResolver jobControlResolver;

    public SignalNormalizationJob(SignalNormalizationService signalNormalizationService,
                                  JobMonitoringService jobMonitoringService,
                                  @Value("${jobs.signal-normalization.batch-size:100}") int batchSize,
                                  JobControlResolver jobControlResolver) {
        super(jobMonitoringService);
        this.signalNormalizationService = signalNormalizationService;
        this.batchSize = batchSize;
        this.jobControlResolver = jobControlResolver;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${jobs.signal-normalization.delay-ms:5000}")
    @SchedulerLock(name = "SignalNormalizationJob", lockAtMostFor = "PT5M")
    public void run() {
        if (!jobControlResolver.isJobEnabled(ManagedJob.SIGNAL_NORMALIZATION)) {
            log.info("SignalNormalizationJob skipped (disabled by admin control).");
            return;
        }
        long processed = executeMonitored("SignalNormalizationJob", () -> {
            String traceId = MDC.get(TraceIdFilter.TRACE_ID);
            log.atInfo()
                    .addKeyValue("event", "signal.normalization.job.started")
                    .addKeyValue("batchSize", batchSize)
                    .addKeyValue("traceId", traceId)
                    .log("Signal normalization job started");

            long count = signalNormalizationService.processPendingSignals(batchSize);

            log.atInfo()
                    .addKeyValue("event", "signal.normalization.job.finished")
                    .addKeyValue("processedCount", count)
                    .addKeyValue("traceId", traceId)
                    .log("Signal normalization job finished");
            return count;
        });

        log.debug("SignalNormalizationJob processed count: {}", processed);
    }
}
