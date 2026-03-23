package com.datarain.pdp.extraction.controller;

import com.datarain.pdp.extraction.dto.CognitiveExtractionResponse;
import com.datarain.pdp.extraction.dto.ClassifyExtractionResponse;
import com.datarain.pdp.extraction.dto.ContextExtractionResponse;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.dto.ExtractionResponse;
import com.datarain.pdp.extraction.dto.FactsExtractionResponse;
import com.datarain.pdp.extraction.dto.IntentExtractionResponse;
import com.datarain.pdp.extraction.dto.ToneExtractionResponse;
import com.datarain.pdp.extraction.dto.TopicsExtractionResponse;
import com.datarain.pdp.extraction.service.ExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/extraction")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "AI / Extraction", description = "AI-driven extraction and classification endpoints for messages.")
public class ExtractionController {

    private final ExtractionService extractionService;

    @PostMapping("/extract")
    @Operation(summary = "Run full extraction pipeline and return all signal groups.")
    public ExtractionResponse extract(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extract(request);
    }

    @PostMapping("/signals")
    @Operation(summary = "Extract combined signal envelope for a message.")
    public ExtractionResponse extractSignals(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractSignals(request);
    }

    @PostMapping("/facts")
    @Operation(summary = "Extract factual entities and activities from a message.")
    public FactsExtractionResponse extractFacts(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractFacts(request);
    }

    @PostMapping("/intent")
    @Operation(summary = "Extract intent signals including goals and plans.")
    public IntentExtractionResponse extractIntent(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractIntent(request);
    }

    @PostMapping("/tone")
    @Operation(summary = "Extract tone and sentiment signals from a message.")
    public ToneExtractionResponse extractTone(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractTone(request);
    }

    @PostMapping("/context")
    @Operation(summary = "Extract context signals from a message.")
    public ContextExtractionResponse extractContext(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractContext(request);
    }

    @PostMapping("/cognitive")
    @Operation(summary = "Extract cognitive signals such as clarity and hesitation.")
    public CognitiveExtractionResponse extractCognitive(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractCognitive(request);
    }

    @PostMapping("/topics")
    @Operation(summary = "Extract topic signals from a message.")
    public TopicsExtractionResponse extractTopics(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractTopics(request);
    }

    @PostMapping("/classify")
//    @PreAuthorize("hasAuthority('ROLE_SYSTEM')")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_SYSTEM')")
    @Operation(summary = "Classify whether a message is useful for signal extraction.")
    public ClassifyExtractionResponse extractClassify(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractClassify(request);
    }
}
