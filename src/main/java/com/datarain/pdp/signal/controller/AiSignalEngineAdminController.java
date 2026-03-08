package com.datarain.pdp.signal.controller;

import com.datarain.pdp.signal.dto.AiSignalEngineRunRequest;
import com.datarain.pdp.signal.dto.AiSignalEngineRunResponse;
import com.datarain.pdp.signal.dto.MessageSignalResponse;
import com.datarain.pdp.signal.service.AiSignalEngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai-signal-engine")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AiSignalEngineAdminController {

    private final AiSignalEngineService aiSignalEngineService;

    @Value("${jobs.ai-signal-engine.batch-size:20}")
    private int defaultBatchSize;

    @Value("${jobs.ai-signal-engine.max-retries:3}")
    private int maxRetries;

    @Value("${pdp.ai.extraction.default-provider}")
    private String defaultProvider;

    @Value("${pdp.ai.extraction.default-model}")
    private String defaultModel;

    @PostMapping("/run")
    public AiSignalEngineRunResponse run(@Valid @RequestBody(required = false) AiSignalEngineRunRequest request) {
        int batchSize = request != null && request.batchSize() != null ? request.batchSize() : defaultBatchSize;
        String provider = request != null && request.provider() != null ? request.provider() : defaultProvider;
        String model = request != null && request.model() != null ? request.model() : defaultModel;

        long processedCount = aiSignalEngineService.processPendingUsefulMessages(batchSize, maxRetries, provider, model);
        return new AiSignalEngineRunResponse(processedCount);
    }

    @GetMapping("/signals")
    public Page<MessageSignalResponse> getSignals(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) UUID userId
    ) {
        return aiSignalEngineService.getSignals(pageable, userId);
    }
}
