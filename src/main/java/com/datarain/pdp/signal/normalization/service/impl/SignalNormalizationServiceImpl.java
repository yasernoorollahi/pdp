package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.service.ActivityNormalizationService;
import com.datarain.pdp.signal.normalization.service.CognitiveLanguageNormalizationService;
import com.datarain.pdp.signal.normalization.service.CognitiveNormalizationService;
import com.datarain.pdp.signal.normalization.service.ContextNormalizationService;
import com.datarain.pdp.signal.normalization.service.EntityNormalizationService;
import com.datarain.pdp.signal.normalization.service.IntentNormalizationService;
import com.datarain.pdp.signal.normalization.service.MetricsAggregationService;
import com.datarain.pdp.signal.normalization.service.PreferenceNormalizationService;
import com.datarain.pdp.signal.normalization.service.ProjectNormalizationService;
import com.datarain.pdp.signal.normalization.service.SignalNormalizationService;
import com.datarain.pdp.signal.normalization.service.SignalParser;
import com.datarain.pdp.signal.normalization.service.ToneNormalizationService;
import com.datarain.pdp.signal.normalization.service.ToolNormalizationService;
import com.datarain.pdp.signal.normalization.service.TopicNormalizationService;
import com.datarain.pdp.signal.repository.MessageSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalNormalizationServiceImpl implements SignalNormalizationService {

    private static final String SYSTEM_EMAIL = "system@pdp.local";

    private final MessageSignalRepository messageSignalRepository;
    private final SignalParser signalParser;
    private final EntityNormalizationService entityNormalizationService;
    private final ActivityNormalizationService activityNormalizationService;
    private final TopicNormalizationService topicNormalizationService;
    private final IntentNormalizationService intentNormalizationService;
    private final PreferenceNormalizationService preferenceNormalizationService;
    private final CognitiveNormalizationService cognitiveNormalizationService;
    private final ToneNormalizationService toneNormalizationService;
    private final ContextNormalizationService contextNormalizationService;
    private final CognitiveLanguageNormalizationService cognitiveLanguageNormalizationService;
    private final ToolNormalizationService toolNormalizationService;
    private final ProjectNormalizationService projectNormalizationService;
    private final MetricsAggregationService metricsAggregationService;
    private final SecurityAuditService securityAuditService;
    private final PdpMetrics metrics;

    @Value("${jobs.signal-normalization.version:1}")
    private int normalizationVersion;

    @Override
    @Transactional
    public long processPendingSignals(int batchSize) {
        int effectiveBatchSize = Math.max(1, batchSize);
        List<MessageSignal> batch = messageSignalRepository.findBatchForNormalization(effectiveBatchSize);
        if (batch.isEmpty()) {
            return 0;
        }

        Instant startedAt = Instant.now();
        messageSignalRepository.markNormalizationStarted(batch.stream().map(MessageSignal::getId).toList(), startedAt);

        long normalizedCount = 0;
        for (MessageSignal signal : batch) {
            UUID signalId = signal.getId();
            String traceId = MDC.get(TraceIdFilter.TRACE_ID);

            log.atInfo()
                    .addKeyValue("event", "signal.normalization.started")
                    .addKeyValue("signalId", signalId)
                    .addKeyValue("messageId", signal.getMessageId())
                    .addKeyValue("userId", signal.getUserId())
                    .addKeyValue("traceId", traceId)
                    .log("Signal normalization started");

            try {
                ParsedSignal parsedSignal = signalParser.parse(signal);

                int entityCount = entityNormalizationService.normalize(signal, parsedSignal);
                int activityCount = activityNormalizationService.normalize(signal, parsedSignal);
                int topicCount = topicNormalizationService.normalize(signal, parsedSignal);
                int intentCount = intentNormalizationService.normalize(signal, parsedSignal);
                int preferenceCount = preferenceNormalizationService.normalize(signal, parsedSignal);
                int cognitiveCount = cognitiveNormalizationService.normalize(signal, parsedSignal);
                int toneCount = toneNormalizationService.normalize(signal, parsedSignal);
                int contextCount = contextNormalizationService.normalize(signal, parsedSignal);
                int cognitiveLanguageCount = cognitiveLanguageNormalizationService.normalize(signal, parsedSignal);
                int toolCount = toolNormalizationService.normalize(signal, parsedSignal);
                int projectCount = projectNormalizationService.normalize(signal, parsedSignal);
                metricsAggregationService.aggregate(signal, parsedSignal);

                messageSignalRepository.markNormalized(signalId, Instant.now(), normalizationVersion);
                normalizedCount++;

                metrics.getSignalNormalizationSuccessCounter().increment();
                metrics.getSignalNormalizationSignalsNormalizedCounter().increment();

                log.atInfo()
                        .addKeyValue("event", "signal.normalization.completed")
                        .addKeyValue("signalId", signalId)
                        .addKeyValue("userId", signal.getUserId())
                        .addKeyValue("entityCount", entityCount)
                        .addKeyValue("activityCount", activityCount)
                        .addKeyValue("topicCount", topicCount)
                        .addKeyValue("intentCount", intentCount)
                        .addKeyValue("preferenceCount", preferenceCount)
                        .addKeyValue("cognitiveCount", cognitiveCount)
                        .addKeyValue("toneCount", toneCount)
                        .addKeyValue("contextCount", contextCount)
                        .addKeyValue("cognitiveLanguageCount", cognitiveLanguageCount)
                        .addKeyValue("toolCount", toolCount)
                        .addKeyValue("projectCount", projectCount)
                        .addKeyValue("traceId", traceId)
                        .log("Signal normalization completed");

                securityAuditService.log(
                        SecurityEventType.SIGNAL_NORMALIZATION_EXECUTED,
                        SYSTEM_EMAIL,
                        signal.getUserId(),
                        null,
                        null,
                        "Signal normalized: " + signalId,
                        true
                );
            } catch (RuntimeException ex) {
                metrics.getSignalNormalizationFailureCounter().increment();

                log.atError()
                        .addKeyValue("event", "signal.normalization.failed")
                        .addKeyValue("signalId", signalId)
                        .addKeyValue("userId", signal.getUserId())
                        .addKeyValue("errorType", ex.getClass().getSimpleName())
                        .addKeyValue("traceId", traceId)
                        .setCause(ex)
                        .log("Signal normalization failed");

                securityAuditService.log(
                        SecurityEventType.SIGNAL_NORMALIZATION_EXECUTED,
                        SYSTEM_EMAIL,
                        signal.getUserId(),
                        null,
                        null,
                        "Signal normalization failed: " + signalId + " error=" + ex.getClass().getSimpleName(),
                        false
                );
            }
        }

        return normalizedCount;
    }
}
