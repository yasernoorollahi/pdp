package com.datarain.pdp.signal.normalization.dto;

import java.util.List;

public record ParsedSignal(
        List<EntitySignal> entities,
        List<ActivitySignal> activities,
        List<TopicSignal> topics,
        List<IntentSignal> intents,
        List<PreferenceSignal> preferences,
        List<CognitiveLanguageSignal> cognitiveLanguages,
        CognitiveSignal cognitive,
        ToneSignal tone,
        ContextSignal context
) {
}
