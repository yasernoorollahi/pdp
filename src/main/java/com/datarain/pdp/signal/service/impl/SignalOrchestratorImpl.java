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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SignalOrchestratorImpl implements SignalOrchestrator {

    private final ExtractionService extractionService;
    private final TaskExecutor taskExecutor;

    public SignalOrchestratorImpl(ExtractionService extractionService,
                                  @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.extractionService = extractionService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public CombinedSignalData orchestrate(ExtractionRequest request) {
        log.atInfo()
                .addKeyValue("event", "signal.orchestrator.started")
                .addKeyValue("provider", request.provider())
                .addKeyValue("model", request.model())
                .log("Signal orchestrator started");

        CompletableFuture<FactsExtractionResponse> factsFuture =
                CompletableFuture.supplyAsync(() -> extractionService.extractFacts(request), taskExecutor);
        CompletableFuture<IntentExtractionResponse> intentFuture =
                CompletableFuture.supplyAsync(() -> extractionService.extractIntent(request), taskExecutor);
        CompletableFuture<ToneExtractionResponse> toneFuture =
                CompletableFuture.supplyAsync(() -> extractionService.extractTone(request), taskExecutor);
        CompletableFuture<ContextExtractionResponse> contextFuture =
                CompletableFuture.supplyAsync(() -> extractionService.extractContext(request), taskExecutor);
        CompletableFuture<CognitiveExtractionResponse> cognitiveFuture =
                CompletableFuture.supplyAsync(() -> extractionService.extractCognitive(request), taskExecutor);
        CompletableFuture<TopicsExtractionResponse> topicsFuture =
                CompletableFuture.supplyAsync(() -> extractionService.extractTopics(request), taskExecutor);

        CompletableFuture.allOf(
                factsFuture,
                intentFuture,
                toneFuture,
                contextFuture,
                cognitiveFuture,
                topicsFuture
        ).join();

        FactsExtractionResponse facts = factsFuture.join();
        IntentExtractionResponse intent = intentFuture.join();
        ToneExtractionResponse tone = toneFuture.join();
        ContextExtractionResponse context = contextFuture.join();
        CognitiveExtractionResponse cognitive = cognitiveFuture.join();
        TopicsExtractionResponse topics = topicsFuture.join();

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
