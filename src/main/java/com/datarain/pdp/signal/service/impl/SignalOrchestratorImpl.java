package com.datarain.pdp.signal.service.impl;

import com.datarain.pdp.extraction.dto.CognitiveExtractionResponse;
import com.datarain.pdp.extraction.dto.ContextExtractionResponse;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.dto.FactsExtractionResponse;
import com.datarain.pdp.extraction.dto.IntentExtractionResponse;
import com.datarain.pdp.extraction.dto.ToneExtractionResponse;
import com.datarain.pdp.extraction.dto.TopicsExtractionResponse;
import com.datarain.pdp.extraction.service.ExtractionService;
import com.datarain.pdp.signal.dto.CombinedSignalData;
import com.datarain.pdp.signal.service.SignalOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignalOrchestratorImpl implements SignalOrchestrator {

    private final ExtractionService extractionService;

    @Override
    public CombinedSignalData orchestrate(ExtractionRequest request) {
        log.atInfo()
                .addKeyValue("event", "signal.orchestrator.started")
                .addKeyValue("provider", request.provider())
                .addKeyValue("model", request.model())
                .log("Signal orchestrator started");

        FactsExtractionResponse facts = extractionService.extractFacts(request);
        IntentExtractionResponse intent = extractionService.extractIntent(request);
        ToneExtractionResponse tone = extractionService.extractTone(request);
        ContextExtractionResponse context = extractionService.extractContext(request);
        CognitiveExtractionResponse cognitive = extractionService.extractCognitive(request);
        TopicsExtractionResponse topics = extractionService.extractTopics(request);

        log.atInfo()
                .addKeyValue("event", "signal.orchestrator.finished")
                .log("Signal orchestrator finished");

        return new CombinedSignalData(
                facts.facts(),
                intent.intent(),
                tone.tone(),
                cognitive.cognitive(),
                context.context(),
                topics.topics()
        );
    }
}
