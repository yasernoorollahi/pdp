package com.datarain.pdp.signal.normalization.dto;

public record ToneSignal(
        String sentiment,
        String mood,
        String motivationLevel,
        String effortPerception,
        Boolean frictionDetected
) {
}
