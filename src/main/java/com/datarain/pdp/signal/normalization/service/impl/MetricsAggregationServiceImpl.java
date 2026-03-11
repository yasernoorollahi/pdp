package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.dto.TopicSignal;
import com.datarain.pdp.signal.normalization.dto.ToneSignal;
import com.datarain.pdp.signal.normalization.entity.IntentType;
import com.datarain.pdp.signal.normalization.entity.PreferenceType;
import com.datarain.pdp.signal.normalization.entity.UserEntityType;
import com.datarain.pdp.signal.normalization.repository.DailyBehaviorMetricRepository;
import com.datarain.pdp.signal.normalization.service.MetricsAggregationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MetricsAggregationServiceImpl implements MetricsAggregationService {

    private final DailyBehaviorMetricRepository dailyBehaviorMetricRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void aggregate(MessageSignal signal, ParsedSignal parsedSignal) {
        LocalDate metricDate = signal.getCreatedAt() == null
                ? LocalDate.now(ZoneOffset.UTC)
                : signal.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();

        int socialMentions = (int) parsedSignal.entities().stream()
                .filter(entity -> entity.entityType() == UserEntityType.PERSON)
                .count();

        int disciplineEvents = countDisciplineEvents(parsedSignal);

        ToneSignal tone = parsedSignal.tone();
        int frictionCount = countFrictions(parsedSignal, tone);
        Double motivationScore = computeMotivationScore(parsedSignal, tone);
        Double energyScore = computeEnergyScore(tone, motivationScore);
        String rawSummary = buildRawSummary(parsedSignal, tone, frictionCount);

        Instant lastSignalAt = signal.getCreatedAt() == null
                ? Instant.now()
                : signal.getCreatedAt();

        dailyBehaviorMetricRepository.upsertMetrics(
                signal.getUserId(),
                metricDate,
                energyScore,
                socialMentions,
                disciplineEvents,
                frictionCount,
                motivationScore,
                rawSummary,
                lastSignalAt
        );
    }

    private int countDisciplineEvents(ParsedSignal parsedSignal) {
        if (parsedSignal.activities() == null || parsedSignal.activities().isEmpty()) {
            return 0;
        }
        int count = 0;
        for (var activity : parsedSignal.activities()) {
            String name = activity.name();
            if (name == null) {
                continue;
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            if (containsAny(normalized, "exercise", "gym", "study", "work", "coding")) {
                count++;
            }
        }
        return count;
    }

    private int countFrictions(ParsedSignal parsedSignal, ToneSignal tone) {
        int count = 0;
        if (tone != null && Boolean.TRUE.equals(tone.frictionDetected())) {
            count++;
        }
        boolean hasDislikes = parsedSignal.preferences().stream()
                .anyMatch(pref -> pref.preferenceType() == PreferenceType.DISLIKE);
        if (hasDislikes) {
            count++;
        }
        if (parsedSignal.cognitive() != null && Boolean.TRUE.equals(parsedSignal.cognitive().hesitationDetected())) {
            count++;
        }
        return count;
    }

    private Double computeMotivationScore(ParsedSignal parsedSignal, ToneSignal tone) {
        Double base = mapMotivationScore(tone == null ? null : tone.motivationLevel());
        boolean hasDriveSignals = parsedSignal.intents().stream()
                .anyMatch(intent -> intent.intentType() == IntentType.GOAL || intent.intentType() == IntentType.COMMITMENT);
        if (base == null) {
            return hasDriveSignals ? 0.6 : null;
        }
        if (hasDriveSignals) {
            return clamp01(base + 0.1);
        }
        return base;
    }

    private Double mapMotivationScore(String motivationLevel) {
        if (motivationLevel == null) {
            return null;
        }
        String normalized = motivationLevel.trim().toLowerCase();
        return switch (normalized) {
            case "high" -> 0.9;
            case "medium" -> 0.6;
            case "low" -> 0.3;
            default -> null;
        };
    }

    private Double computeEnergyScore(ToneSignal tone, Double motivationScore) {
        List<Double> moodScores = new ArrayList<>();
        if (tone != null && tone.mood() != null) {
            String mood = tone.mood().toLowerCase(Locale.ROOT);
            if (containsAny(mood, "energized", "excited")) {
                moodScores.add(0.9);
            }
            if (containsAny(mood, "motivated")) {
                moodScores.add(0.8);
            }
            if (containsAny(mood, "tired")) {
                moodScores.add(0.3);
            }
            if (containsAny(mood, "drained", "exhausted")) {
                moodScores.add(0.2);
            }
        }
        Double moodScore = moodScores.isEmpty()
                ? null
                : moodScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        Double combined = null;
        if (moodScore != null && motivationScore != null) {
            combined = (moodScore + motivationScore) / 2.0;
        } else if (moodScore != null) {
            combined = moodScore;
        } else if (motivationScore != null) {
            combined = motivationScore;
        }

        if (tone != null && tone.sentiment() != null && combined != null) {
            String sentiment = tone.sentiment().toLowerCase(Locale.ROOT);
            if (containsAny(sentiment, "positive")) {
                combined = clamp01(combined + 0.05);
            } else if (containsAny(sentiment, "negative")) {
                combined = clamp01(combined - 0.05);
            }
        }
        return combined;
    }

    private String buildRawSummary(ParsedSignal parsedSignal, ToneSignal tone, int frictionCount) {
        ObjectNode node = objectMapper.createObjectNode();
        if (tone != null && tone.sentiment() != null) {
            node.put("sentiment", tone.sentiment());
        }
        if (tone != null && tone.mood() != null) {
            node.put("mood", tone.mood());
        }
        if (tone != null && tone.motivationLevel() != null) {
            node.put("motivation_level", tone.motivationLevel());
        }
        if (tone != null && tone.effortPerception() != null) {
            node.put("effort_perception", tone.effortPerception());
        }
        if (tone != null && tone.frictionDetected() != null) {
            node.put("friction_detected", tone.frictionDetected());
        }
        node.put("friction_count", frictionCount);
        node.put("activities", parsedSignal.activities().size());
        if (!parsedSignal.topics().isEmpty()) {
            var topicsArray = node.putArray("topics");
            parsedSignal.topics().stream()
                    .map(TopicSignal::topic)
                    .filter(value -> value != null && !value.isBlank())
                    .distinct()
                    .forEach(value -> topicsArray.add(value));
        }
        return node.isEmpty() ? null : node.toString();
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
