package com.datarain.pdp.signal.normalization.dto;

public record CognitiveSignal(
        String clarityLevel,
        String decisionState,
        Boolean hesitationDetected
) {
}
