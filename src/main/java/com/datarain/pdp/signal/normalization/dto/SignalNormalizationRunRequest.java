package com.datarain.pdp.signal.normalization.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SignalNormalizationRunRequest(
        @Min(1) @Max(5000) Integer batchSize
) {
}
