package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.dto.ToneSignal;
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

        int disciplineEvents = parsedSignal.activities().size();

        ToneSignal tone = parsedSignal.tone();
        int frictionCount = tone != null && Boolean.TRUE.equals(tone.frictionDetected()) ? 1 : 0;
        Double motivationScore = mapMotivationScore(tone == null ? null : tone.motivationLevel());
        String rawSummary = buildRawSummary(tone);

        Instant lastSignalAt = signal.getCreatedAt() == null
                ? Instant.now()
                : signal.getCreatedAt();

        dailyBehaviorMetricRepository.upsertMetrics(
                signal.getUserId(),
                metricDate,
                socialMentions,
                disciplineEvents,
                frictionCount,
                motivationScore,
                rawSummary,
                lastSignalAt
        );
    }

    private Double mapMotivationScore(String motivationLevel) {
        if (motivationLevel == null) {
            return null;
        }
        String normalized = motivationLevel.trim().toLowerCase();
        return switch (normalized) {
            case "high" -> 0.8;
            case "medium" -> 0.5;
            case "low" -> 0.2;
            default -> null;
        };
    }

    private String buildRawSummary(ToneSignal tone) {
        if (tone == null) {
            return null;
        }
        ObjectNode node = objectMapper.createObjectNode();
        if (tone.sentiment() != null) {
            node.put("sentiment", tone.sentiment());
        }
        if (tone.mood() != null) {
            node.put("mood", tone.mood());
        }
        if (tone.motivationLevel() != null) {
            node.put("motivation_level", tone.motivationLevel());
        }
        if (tone.effortPerception() != null) {
            node.put("effort_perception", tone.effortPerception());
        }
        if (tone.frictionDetected() != null) {
            node.put("friction_detected", tone.frictionDetected());
        }
        return node.isEmpty() ? null : node.toString();
    }
}
