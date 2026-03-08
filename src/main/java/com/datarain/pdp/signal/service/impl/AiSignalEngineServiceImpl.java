package com.datarain.pdp.signal.service.impl;

import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.message.entity.MessageProcessingStatus;
import com.datarain.pdp.message.entity.UserMessage;
import com.datarain.pdp.message.repository.UserMessageRepository;
import com.datarain.pdp.signal.dto.CombinedSignalData;
import com.datarain.pdp.signal.dto.MessageSignalResponse;
import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.mapper.MessageSignalMapper;
import com.datarain.pdp.signal.repository.MessageSignalRepository;
import com.datarain.pdp.signal.service.AiSignalEngineService;
import com.datarain.pdp.signal.service.SignalOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiSignalEngineServiceImpl implements AiSignalEngineService {

    private static final String SYSTEM_EMAIL = "system@pdp.local";

    private final UserMessageRepository userMessageRepository;
    private final MessageSignalRepository messageSignalRepository;
    private final SignalOrchestrator signalOrchestrator;
    private final MessageSignalMapper messageSignalMapper;
    private final SecurityAuditService securityAuditService;
    private final PdpMetrics metrics;
    private final ObjectMapper objectMapper;

    @Value("${jobs.ai-signal-engine.pipeline-version:ai-signal-engine-v1}")
    private String pipelineVersion;

    @Override
    @Transactional
    public long processPendingUsefulMessages(int batchSize, int maxRetries, String provider, String model) {
        Page<UserMessage> page = userMessageRepository.findProcessableUsefulMessages(
                EnumSet.of(MessageProcessingStatus.PENDING, MessageProcessingStatus.FAILED),
                Math.max(1, maxRetries),
                PageRequest.of(0, Math.max(1, batchSize))
        );

        long processedCount = 0;
        for (UserMessage message : page.getContent()) {
            processSingleMessage(message, provider, model);
            processedCount++;
        }

        log.atInfo()
                .addKeyValue("event", "ai.signal.engine.batch.finished")
                .addKeyValue("processedCount", processedCount)
                .addKeyValue("requestedBatchSize", batchSize)
                .log("AI signal engine batch finished");

        return processedCount;
    }

    @Override
    public Page<MessageSignalResponse> getSignals(Pageable pageable, UUID userId) {
        Page<MessageSignal> page = (userId == null)
                ? messageSignalRepository.findAll(pageable)
                : messageSignalRepository.findAllByUserId(userId, pageable);

        return page.map(messageSignalMapper::toResponse);
    }

    private void processSingleMessage(UserMessage message, String provider, String model) {
        Instant startedAt = Instant.now();
        markProcessingStarted(message, startedAt);

        try {
            ExtractionRequest request = new ExtractionRequest(message.getContent(), provider, model);
            CombinedSignalData combined = signalOrchestrator.orchestrate(request);
            Instant finishedAt = Instant.now();
            int latencyMs = (int) Duration.between(startedAt, finishedAt).toMillis();

            storeSignals(message, combined, model, latencyMs);

            message.setProcessed(true);
            message.setProcessedAt(finishedAt);
            message.setProcessingStatus(MessageProcessingStatus.DONE);
            message.setSignalReason(null);
            userMessageRepository.save(message);

            metrics.getUserMessageProcessedCounter().increment();
            metrics.getSignalEngineSuccessCounter().increment();

            log.atInfo()
                    .addKeyValue("event", "ai.signal.engine.message.done")
                    .addKeyValue("messageId", message.getId())
                    .addKeyValue("userId", message.getUserId())
                    .addKeyValue("retryCount", message.getRetryCount())
                    .addKeyValue("latencyMs", latencyMs)
                    .log("AI signal engine processed message");

            securityAuditService.log(
                    SecurityEventType.SIGNAL_ENGINE_EXECUTED,
                    SYSTEM_EMAIL,
                    null,
                    null,
                    null,
                    "Signal engine processed message: " + message.getId(),
                    true
            );
        } catch (RuntimeException ex) {
            message.setRetryCount(message.getRetryCount() + 1);
            message.setProcessingStatus(MessageProcessingStatus.FAILED);
            message.setProcessed(false);
            message.setProcessedAt(Instant.now());
            message.setSignalReason(sanitize(ex.getMessage()));
            userMessageRepository.save(message);

            metrics.getSignalEngineFailureCounter().increment();

            log.atWarn()
                    .addKeyValue("event", "ai.signal.engine.message.failed")
                    .addKeyValue("messageId", message.getId())
                    .addKeyValue("userId", message.getUserId())
                    .addKeyValue("retryCount", message.getRetryCount())
                    .addKeyValue("errorType", ex.getClass().getSimpleName())
                    .log("AI signal engine failed to process message");

            securityAuditService.log(
                    SecurityEventType.SIGNAL_ENGINE_EXECUTED,
                    SYSTEM_EMAIL,
                    null,
                    null,
                    null,
                    "Signal engine failed for message: " + message.getId() + " error=" + ex.getClass().getSimpleName(),
                    false
            );
        }
    }

    private void markProcessingStarted(UserMessage message, Instant startedAt) {
        message.setProcessingStartedAt(startedAt);
        message.setProcessingStatus(MessageProcessingStatus.PROCESSING);
        userMessageRepository.save(message);
    }

    private void storeSignals(UserMessage message, CombinedSignalData combined, String model, int latencyMs) {
        int nextVersion = messageSignalRepository
                .findTopByMessageIdOrderBySignalVersionDesc(message.getId())
                .map(existing -> existing.getSignalVersion() + 1)
                .orElse(1);

        MessageSignal messageSignal = new MessageSignal();
        messageSignal.setMessageId(message.getId());
        messageSignal.setUserId(message.getUserId());
        messageSignal.setSignalVersion(nextVersion);
        messageSignal.setSignals(objectMapper.valueToTree(combined));
        messageSignal.setExtractorModel(model);
        messageSignal.setExtractionLatencyMs(latencyMs);
        messageSignal.setPipelineVersion(pipelineVersion);
        messageSignalRepository.save(messageSignal);

        metrics.getSignalEngineSignalsStoredCounter().increment();
    }

    private String sanitize(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
