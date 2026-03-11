package com.datarain.pdp.signal.normalization.repository;

public interface DailyBehaviorMetricSummaryProjection {
    Double getAvgEnergy();
    Double getAvgMotivation();
    Long getFrictionSum();
    Long getSocialSum();
    Long getDisciplineSum();
}
