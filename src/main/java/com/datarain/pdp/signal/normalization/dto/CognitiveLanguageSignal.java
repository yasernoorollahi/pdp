package com.datarain.pdp.signal.normalization.dto;

import com.datarain.pdp.signal.normalization.entity.CognitiveLanguageType;

public record CognitiveLanguageSignal(
        String value,
        CognitiveLanguageType type
) {
}
