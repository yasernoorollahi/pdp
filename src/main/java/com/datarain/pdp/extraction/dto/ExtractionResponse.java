package com.datarain.pdp.extraction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExtractionResponse(
        @NotNull List<@Valid EventItem> events,
        @NotNull List<@Valid EmotionItem> emotions,
        @NotNull List<@Valid IntentItem> intents,
        @DecimalMin("0.0") @DecimalMax("1.0") double confidence,
        @NotBlank String overallSentiment,
        String dominantEmotion,
        String dominantIntent,
        @Valid MetadataItem metadata
) {

    public record EventItem(
            @NotBlank String eventType,
            @NotBlank String category,
            @NotBlank String evidence,
            @NotNull List<String> actors,
            String location,
            String temporalRef,
            String sentiment,
            @DecimalMin("0.0") @DecimalMax("1.0") double confidence
    ) {
    }

    public record EmotionItem(
            @NotBlank String emotion,
            @NotBlank String primaryEmotion,
            @DecimalMin("0.0") @DecimalMax("1.0") double intensity,
            @DecimalMin("-1.0") @DecimalMax("1.0") double valence,
            @DecimalMin("0.0") @DecimalMax("1.0") double arousal,
            @NotBlank String evidence,
            @DecimalMin("0.0") @DecimalMax("1.0") double confidence
    ) {
    }

    public record IntentItem(
            @NotBlank String intentType,
            @NotBlank String intentCategory,
            @NotBlank String evidence,
            @DecimalMin("0.0") @DecimalMax("1.0") double urgency,
            @DecimalMin("0.0") @DecimalMax("1.0") double confidence
    ) {
    }

    public record MetadataItem(
            @NotBlank String languageDetected,
            int textLength,
            @NotNull List<String> processingNotes
    ) {
    }
}
