package com.datarain.pdp.signal.normalization.controller;

import com.datarain.pdp.signal.normalization.dto.SignalNormalizationRunRequest;
import com.datarain.pdp.signal.normalization.dto.SignalNormalizationRunResponse;
import com.datarain.pdp.signal.normalization.service.SignalNormalizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/signal-normalization")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class SignalNormalizationAdminController {

    private final SignalNormalizationService signalNormalizationService;

    @Value("${jobs.signal-normalization.batch-size:100}")
    private int defaultBatchSize;

    @PostMapping("/run")
    public SignalNormalizationRunResponse run(@Valid @RequestBody(required = false) SignalNormalizationRunRequest request) {
        int batchSize = request != null && request.batchSize() != null
                ? request.batchSize()
                : defaultBatchSize;

        long processedCount = signalNormalizationService.processPendingSignals(batchSize);
        return new SignalNormalizationRunResponse(processedCount);
    }
}
