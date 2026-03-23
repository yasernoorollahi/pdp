package com.datarain.pdp.signal.service.model;

import java.time.LocalDate;

public record DailyBehaviorMetricView(
        LocalDate metricDate,
        Double energyScore,
        Double motivationScore,
        Integer frictionCount,
        Integer socialMentionsCount,
        Integer disciplineEventsCount
) {
}
