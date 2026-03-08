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
public class ExtractionController {

    private final ExtractionService extractionService;

    @PostMapping("/extract")
    public ExtractionResponse extract(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extract(request);
    }

    @PostMapping("/signals")
    public ExtractionResponse extractSignals(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractSignals(request);
    }

    @PostMapping("/facts")
    public FactsExtractionResponse extractFacts(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractFacts(request);
    }

    @PostMapping("/intent")
    public IntentExtractionResponse extractIntent(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractIntent(request);
    }

    @PostMapping("/tone")
    public ToneExtractionResponse extractTone(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractTone(request);
    }

    @PostMapping("/context")
    public ContextExtractionResponse extractContext(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractContext(request);
    }

    @PostMapping("/cognitive")
    public CognitiveExtractionResponse extractCognitive(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractCognitive(request);
    }

    @PostMapping("/topics")
    public TopicsExtractionResponse extractTopics(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractTopics(request);
    }

    @PostMapping("/classify")
//    @PreAuthorize("hasAuthority('ROLE_SYSTEM')")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_SYSTEM')")
    public ClassifyExtractionResponse extractClassify(@Valid @RequestBody ExtractionRequest request) {
        return extractionService.extractClassify(request);
    }
}
