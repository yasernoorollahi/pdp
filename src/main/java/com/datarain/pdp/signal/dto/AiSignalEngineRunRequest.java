package com.datarain.pdp.signal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AiSignalEngineRunRequest(
        @Min(1) @Max(500) Integer batchSize,
        @Size(max = 50) String provider,
        @Size(max = 100) String model
) {
}
