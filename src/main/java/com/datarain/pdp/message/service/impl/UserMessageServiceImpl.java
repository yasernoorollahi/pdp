package com.datarain.pdp.message.service.impl;

import com.datarain.pdp.exception.business.UserMessageNotFoundException;
import com.datarain.pdp.extraction.dto.ClassifyExtractionResponse;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.service.ExtractionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import com.datarain.pdp.message.dto.UserMessageCreateRequest;
import com.datarain.pdp.message.dto.UserMessageProcessedRequest;
import com.datarain.pdp.message.dto.UserMessageResponse;
import com.datarain.pdp.message.dto.UserMessageUpdateRequest;
import com.datarain.pdp.message.entity.MessageAnalysisStatus;
import com.datarain.pdp.message.entity.MessageProcessingStatus;
import com.datarain.pdp.message.entity.UserMessage;
import com.datarain.pdp.message.mapper.UserMessageMapper;
import com.datarain.pdp.message.repository.UserMessageRepository;
import com.datarain.pdp.message.service.UserMessageService;
import com.datarain.pdp.message.specification.UserMessageSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMessageServiceImpl implements UserMessageService {

    private final UserMessageRepository userMessageRepository;
    private final UserMessageMapper userMessageMapper;
    private final ExtractionService extractionService;
    private final SecurityAuditService securityAuditService;
    private final PdpMetrics metrics;

    @Override
    @Transactional
    public UserMessageResponse create(UserMessageCreateRequest request) {
        UUID currentUserId = SecurityUtils.currentUserId();
        String currentEmail = SecurityUtils.currentUsername();

        UserMessage userMessage = userMessageMapper.toEntity(request);
        userMessage.setUserId(currentUserId);
        userMessage.setProcessed(false);
        userMessage.setAnalysisStatus(MessageAnalysisStatus.PENDING);
        userMessage.setSignalDecision(null);
        userMessage.setSignalScore(null);
        userMessage.setSignalReason(null);
        userMessage.setProcessedAt(null);
        userMessage.setProcessingStatus(MessageProcessingStatus.PENDING);
        userMessage.setRetryCount(0);
        userMessage.setProcessingStartedAt(null);

        UserMessage saved = userMessageRepository.save(userMessage);
        metrics.getUserMessageCreatedCounter().increment();

        log.atInfo()
                .addKeyValue("event", "user.message.created")
                .addKeyValue("messageId", saved.getId())
                .addKeyValue("userId", currentUserId)
                .addKeyValue("messageDate", saved.getMessageDate())
                .log("User message created");

        securityAuditService.log(
                SecurityEventType.USER_MESSAGE_CREATED,
                currentEmail,
                currentUserId,
                null,
                null,
                "User message created: " + saved.getId(),
                true
        );

        return userMessageMapper.toResponse(saved);
    }

    @Override
    public UserMessageResponse getById(UUID id) {
        return userMessageMapper.toResponse(findOwnedMessage(id));
    }

    @Override
    @Transactional
    public UserMessageResponse update(UUID id, UserMessageUpdateRequest request) {
        UserMessage userMessage = findOwnedMessage(id);

        userMessage.setContent(request.content());
        userMessage.setMessageDate(request.messageDate());
        userMessage.setProcessed(false);
        userMessage.setAnalysisStatus(MessageAnalysisStatus.PENDING);
        userMessage.setSignalDecision(null);
        userMessage.setSignalScore(null);
        userMessage.setSignalReason(null);
        userMessage.setProcessedAt(null);
        userMessage.setProcessingStatus(MessageProcessingStatus.PENDING);
        userMessage.setRetryCount(0);
        userMessage.setProcessingStartedAt(null);
        UserMessage saved = userMessageRepository.save(userMessage);

        UUID currentUserId = SecurityUtils.currentUserId();
        String currentEmail = SecurityUtils.currentUsername();
        metrics.getUserMessageUpdatedCounter().increment();

        log.atInfo()
                .addKeyValue("event", "user.message.updated")
                .addKeyValue("messageId", saved.getId())
                .addKeyValue("userId", currentUserId)
                .addKeyValue("messageDate", saved.getMessageDate())
                .log("User message updated");

        securityAuditService.log(
                SecurityEventType.USER_MESSAGE_UPDATED,
                currentEmail,
                currentUserId,
                null,
                null,
                "User message updated: " + saved.getId(),
                true
        );

        return userMessageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserMessageResponse setProcessed(UUID id, UserMessageProcessedRequest request) {
        UserMessage userMessage = findOwnedMessage(id);
        userMessage.setProcessed(request.processed());
        UserMessage saved = userMessageRepository.save(userMessage);

        UUID currentUserId = SecurityUtils.currentUserId();
        String currentEmail = SecurityUtils.currentUsername();
        if (request.processed()) {
            metrics.getUserMessageProcessedCounter().increment();
        }

        log.atInfo()
                .addKeyValue("event", "user.message.processed.changed")
                .addKeyValue("messageId", saved.getId())
                .addKeyValue("userId", currentUserId)
                .addKeyValue("processed", saved.isProcessed())
                .log("User message processed state changed");

        securityAuditService.log(
                SecurityEventType.USER_MESSAGE_PROCESSED,
                currentEmail,
                currentUserId,
                null,
                null,
                "User message processed state changed to " + saved.isProcessed() + ": " + saved.getId(),
                true
        );

        return userMessageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UserMessage userMessage = findOwnedMessage(id);
        UUID currentUserId = SecurityUtils.currentUserId();
        String currentEmail = SecurityUtils.currentUsername();

        userMessageRepository.delete(userMessage);
        metrics.getUserMessageDeletedCounter().increment();

        log.atInfo()
                .addKeyValue("event", "user.message.deleted")
                .addKeyValue("messageId", id)
                .addKeyValue("userId", currentUserId)
                .log("User message deleted");

        securityAuditService.log(
                SecurityEventType.USER_MESSAGE_DELETED,
                currentEmail,
                currentUserId,
                null,
                null,
                "User message deleted: " + id,
                true
        );
    }

    @Override
    public Page<UserMessageResponse> getAll(Pageable pageable, Boolean processed, LocalDate fromDate, LocalDate toDate) {
        UUID currentUserId = SecurityUtils.currentUserId();

        Specification<UserMessage> spec = UserMessageSpecification.hasUserId(currentUserId);
        if (processed != null) {
            spec = spec.and(UserMessageSpecification.hasProcessed(processed));
        }
        if (fromDate != null) {
            spec = spec.and(UserMessageSpecification.messageDateFrom(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(UserMessageSpecification.messageDateTo(toDate));
        }

        log.atInfo()
                .addKeyValue("event", "user.message.list.requested")
                .addKeyValue("userId", currentUserId)
                .addKeyValue("processed", processed)
                .addKeyValue("fromDate", fromDate)
                .addKeyValue("toDate", toDate)
                .addKeyValue("page", pageable.getPageNumber())
                .addKeyValue("size", pageable.getPageSize())
                .log("User message list requested");

        return userMessageRepository.findAll(spec, pageable)
                .map(userMessageMapper::toResponse);
    }

    @Override
    @Transactional
    public long analyzePendingMessages(int batchSize, String provider, String model) {
        Page<UserMessage> page = userMessageRepository.findByAnalysisStatusOrderByCreatedAtAsc(
                MessageAnalysisStatus.PENDING,
                PageRequest.of(0, Math.max(1, batchSize))
        );

        long processedCount = 0;
        for (UserMessage message : page.getContent()) {
            processSingleMessage(message, provider, model);
            processedCount++;
        }

        log.atInfo()
                .addKeyValue("event", "user.message.analysis.batch.finished")
                .addKeyValue("processedCount", processedCount)
                .addKeyValue("requestedBatchSize", batchSize)
                .log("User message analysis batch finished");

        return processedCount;
    }

    private UserMessage findOwnedMessage(UUID id) {
        UUID currentUserId = SecurityUtils.currentUserId();
        return userMessageRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new UserMessageNotFoundException(id));
    }

    private void processSingleMessage(UserMessage message, String provider, String model) {
        Instant now = Instant.now();
        try {
            ClassifyExtractionResponse response = extractionService.extractClassify(
                    new ExtractionRequest(message.getContent(), provider, model)
            );
            ClassificationResult result = mapClassification(response.classify());
            boolean useful = "USEFUL".equalsIgnoreCase(result.signalDecision());

            message.setAnalysisStatus(result.analysisStatus());
            message.setSignalDecision(result.signalDecision());
            message.setSignalScore(result.signalScore());
            message.setSignalReason(result.signalReason());
            message.setRetryCount(0);
            message.setProcessingStatus(useful ? MessageProcessingStatus.PENDING : MessageProcessingStatus.DONE);
            message.setProcessingStartedAt(null);
            message.setProcessed(useful ? false : result.analysisStatus() != MessageAnalysisStatus.FAILED);
            message.setProcessedAt(useful ? null : now);
            userMessageRepository.save(message);

            if (!useful && result.analysisStatus() != MessageAnalysisStatus.FAILED) {
                metrics.getUserMessageProcessedCounter().increment();
            }

            log.atInfo()
                    .addKeyValue("event", "user.message.analysis.completed")
                    .addKeyValue("messageId", message.getId())
                    .addKeyValue("userId", message.getUserId())
                    .addKeyValue("analysisStatus", result.analysisStatus())
                    .addKeyValue("signalDecision", result.signalDecision())
                    .addKeyValue("signalScore", result.signalScore())
                    .log("User message analyzed");

            securityAuditService.log(
                    SecurityEventType.USER_MESSAGE_PROCESSED,
                    "system@pdp.local",
                    null,
                    null,
                    null,
                    "User message analyzed by classifier: " + message.getId() + " status=" + result.analysisStatus(),
                    true
            );
        } catch (RuntimeException ex) {
            message.setAnalysisStatus(MessageAnalysisStatus.FAILED);
            message.setProcessedAt(now);
            message.setProcessed(false);
            message.setSignalReason(sanitizeReason(ex.getMessage()));
            message.setRetryCount(message.getRetryCount() + 1);
            message.setProcessingStatus(MessageProcessingStatus.FAILED);
            message.setProcessingStartedAt(now);
            userMessageRepository.save(message);

            log.atWarn()
                    .addKeyValue("event", "user.message.analysis.failed")
                    .addKeyValue("messageId", message.getId())
                    .addKeyValue("userId", message.getUserId())
                    .addKeyValue("errorType", ex.getClass().getSimpleName())
                    .log("User message analysis failed");

            securityAuditService.log(
                    SecurityEventType.USER_MESSAGE_PROCESSED,
                    "system@pdp.local",
                    null,
                    null,
                    null,
                    "User message classifier failed: " + message.getId() + " error=" + ex.getClass().getSimpleName(),
                    false
            );
        }
    }

    private ClassificationResult mapClassification(JsonNode classify) {
        Boolean worthExtracting = readBoolean(classify, "worthExtracting", "shouldExtract", "valuable", "extractable");
        String decision = readText(classify, "signalDecision", "decision", "label", "classification");
        Double score = readDouble(classify, "signalScore", "score", "confidence", "probability");
        String reason = readText(classify, "signalReason", "reason", "explanation");

        MessageAnalysisStatus status;
        if (worthExtracting != null) {
            status = worthExtracting ? MessageAnalysisStatus.ANALYZED : MessageAnalysisStatus.SKIPPED;
        } else if (isSkipDecision(decision)) {
            status = MessageAnalysisStatus.SKIPPED;
        } else {
            status = MessageAnalysisStatus.ANALYZED;
        }

        String normalizedDecision = normalizeDecision(decision, status);
        return new ClassificationResult(status, normalizedDecision, score, reason);
    }

    private boolean isSkipDecision(String decision) {
        if (decision == null || decision.isBlank()) {
            return false;
        }
        String normalized = decision.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("SKIP")
                || normalized.equals("SKIPPED")
                || normalized.equals("NO_EXTRACT")
                || normalized.equals("NOT_WORTH")
                || normalized.equals("NOT_WORTH_EXTRACTING");
    }

    private String normalizeDecision(String decision, MessageAnalysisStatus status) {
        if (decision != null && !decision.isBlank()) {
            String normalized = decision.trim().toUpperCase(Locale.ROOT);
            if (normalized.equals("USEFUL")
                    || normalized.equals("ANALYZED")
                    || normalized.equals("EXTRACT")
                    || normalized.equals("ALLOW")) {
                return "USEFUL";
            }
            if (normalized.equals("IGNORE")
                    || normalized.equals("SKIP")
                    || normalized.equals("SKIPPED")
                    || normalized.equals("NO_EXTRACT")
                    || normalized.equals("NOT_WORTH")
                    || normalized.equals("NOT_WORTH_EXTRACTING")) {
                return "IGNORE";
            }
            return normalized;
        }
        return status == MessageAnalysisStatus.SKIPPED ? "IGNORE" : "USEFUL";
    }

    private String sanitizeReason(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.length() <= 1000 ? raw : raw.substring(0, 1000);
    }

    private String readText(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && value.isTextual() && !value.asText().isBlank()) {
                return value.asText().trim();
            }
        }
        return null;
    }

    private Double readDouble(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && value.isNumber()) {
                return value.doubleValue();
            }
            if (value != null && value.isTextual()) {
                try {
                    return Double.parseDouble(value.asText().trim());
                } catch (NumberFormatException ignored) {
                    // ignore invalid value
                }
            }
        }
        return null;
    }

    private Boolean readBoolean(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && value.isBoolean()) {
                return value.booleanValue();
            }
            if (value != null && value.isTextual()) {
                String normalized = value.asText().trim().toLowerCase(Locale.ROOT);
                if (Objects.equals(normalized, "true")) {
                    return true;
                }
                if (Objects.equals(normalized, "false")) {
                    return false;
                }
            }
        }
        return null;
    }

    private record ClassificationResult(
            MessageAnalysisStatus analysisStatus,
            String signalDecision,
            Double signalScore,
            String signalReason
    ) {
    }
}
