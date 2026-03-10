package com.datarain.pdp.signal.normalization.dto;

import com.datarain.pdp.signal.normalization.entity.IntentType;

public record IntentSignal(
        IntentType intentType,
        String description,
        String temporalScope
) {
}
