package com.datarain.pdp.extraction.service.impl;

import com.datarain.pdp.extraction.dto.CognitiveExtractionResponse;
import com.datarain.pdp.extraction.dto.ClassifyExtractionResponse;
import com.datarain.pdp.extraction.dto.ContextExtractionResponse;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.dto.ExtractionResponse;
import com.datarain.pdp.extraction.dto.FactsExtractionResponse;
import com.datarain.pdp.extraction.dto.IntentExtractionResponse;
import com.datarain.pdp.extraction.dto.ToneExtractionResponse;
import com.datarain.pdp.extraction.dto.TopicsExtractionResponse;
import com.datarain.pdp.extraction.mapper.ExtractionMapper;
import com.datarain.pdp.extraction.repository.ExtractionRepository;
import com.datarain.pdp.extraction.repository.dto.AiExtractionResponse;
import com.datarain.pdp.extraction.service.ExtractionService;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExtractionServiceImpl implements ExtractionService {

    private final ExtractionRepository extractionRepository;
    private final ExtractionMapper extractionMapper;
    private final PdpMetrics metrics;
    private final SecurityAuditService securityAuditService;

    @Override
    public ExtractionResponse extract(ExtractionRequest request) {
        return extractSignals(request);
    }

    @Override
    public ExtractionResponse extractSignals(ExtractionRequest request) {
        return invokeEndpoint("signals", request, () -> {
            AiExtractionResponse aiResponse = extractionRepository.extractSignals(request);
            return extractionMapper.toResponse(aiResponse);
        });
    }

    @Override
    public FactsExtractionResponse extractFacts(ExtractionRequest request) {
        return invokeEndpoint("facts", request, () -> new FactsExtractionResponse(extractionRepository.extractFacts(request)));
    }

    @Override
    public IntentExtractionResponse extractIntent(ExtractionRequest request) {
        return invokeEndpoint("intent", request, () -> new IntentExtractionResponse(extractionRepository.extractIntent(request)));
    }

    @Override
    public ToneExtractionResponse extractTone(ExtractionRequest request) {
        return invokeEndpoint("tone", request, () -> new ToneExtractionResponse(extractionRepository.extractTone(request)));
    }

    @Override
    public ContextExtractionResponse extractContext(ExtractionRequest request) {
        return invokeEndpoint("context", request, () -> new ContextExtractionResponse(extractionRepository.extractContext(request)));
    }

    @Override
    public CognitiveExtractionResponse extractCognitive(ExtractionRequest request) {
        return invokeEndpoint("cognitive", request, () -> new CognitiveExtractionResponse(extractionRepository.extractCognitive(request)));
    }

    @Override
    public TopicsExtractionResponse extractTopics(ExtractionRequest request) {
        return invokeEndpoint("topics", request, () -> new TopicsExtractionResponse(extractionRepository.extractTopics(request)));
    }

    @Override
    public ClassifyExtractionResponse extractClassify(ExtractionRequest request) {
        return invokeEndpoint("classify", request, () -> new ClassifyExtractionResponse(extractionRepository.extractClassify(request)));
    }

    private <T> T invokeEndpoint(String endpoint, ExtractionRequest request, Supplier<T> action) {
        Actor actor = currentActor();
        metrics.getExtractionRequestedCounter().increment();

        log.atInfo()
                .addKeyValue("event", "extraction.requested")
                .addKeyValue("endpoint", endpoint)
                .addKeyValue("userId", actor.userId())
                .addKeyValue("textLength", request.text().length())
                .addKeyValue("provider", request.provider())
                .addKeyValue("model", request.model())
                .log("Extraction request received");

        try {
            T response = action.get();

            securityAuditService.log(
                    SecurityEventType.EXTRACTION_REQUESTED,
                    actor.email(),
                    actor.userId(),
                    null,
                    null,
                    "Extraction requested for endpoint: " + endpoint,
                    true
            );

            return response;
        } catch (RuntimeException ex) {
            metrics.getExtractionFailedCounter().increment();
            securityAuditService.log(
                    SecurityEventType.EXTRACTION_REQUESTED,
                    actor.email(),
                    actor.userId(),
                    null,
                    null,
                    "Extraction endpoint " + endpoint + " failed: " + ex.getClass().getSimpleName(),
                    false
            );
            throw ex;
        }
    }

    private Actor currentActor() {
        if (SecurityUtils.isAuthenticated()) {
            return new Actor(SecurityUtils.currentUserId(), SecurityUtils.currentUsername());
        }
        return new Actor(null, "system@pdp.local");
    }

    private record Actor(UUID userId, String email) {
    }
}
