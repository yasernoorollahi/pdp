package com.datarain.pdp.extraction.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiExtractionResponse(
        List<AiEventItem> events,
        List<AiEmotionItem> emotions,
        List<AiIntentItem> intents,
        double confidence,
        @JsonProperty("overall_sentiment") String overallSentiment,
        @JsonProperty("dominant_emotion") String dominantEmotion,
        @JsonProperty("dominant_intent") String dominantIntent,
        AiMetadataItem metadata
) {

    public record AiEventItem(
            @JsonProperty("event_type") String eventType,
            String category,
            String evidence,
            List<String> actors,
            String location,
            @JsonProperty("temporal_ref") String temporalRef,
            String sentiment,
            double confidence
    ) {
    }

    public record AiEmotionItem(
            String emotion,
            @JsonProperty("primary_emotion") String primaryEmotion,
            double intensity,
            double valence,
            double arousal,
            String evidence,
            double confidence
    ) {
    }

    public record AiIntentItem(
            @JsonProperty("intent_type") String intentType,
            @JsonProperty("intent_category") String intentCategory,
            String evidence,
            double urgency,
            double confidence
    ) {
    }

    public record AiMetadataItem(
            @JsonProperty("language_detected") String languageDetected,
            @JsonProperty("text_length") int textLength,
            @JsonProperty("processing_notes") List<String> processingNotes
    ) {
    }
}
