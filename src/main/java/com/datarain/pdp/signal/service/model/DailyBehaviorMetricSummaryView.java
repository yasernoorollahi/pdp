package com.datarain.pdp.signal.service.model;

public record DailyBehaviorMetricSummaryView(
        Double avgEnergy,
        Double avgMotivation,
        Long frictionSum,
        Long socialSum,
        Long disciplineSum
) {
    public static DailyBehaviorMetricSummaryView empty() {
        return new DailyBehaviorMetricSummaryView(null, null, 0L, 0L, 0L);
    }
}
