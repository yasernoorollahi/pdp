package com.datarain.pdp.insights.mapper;

import com.datarain.pdp.insights.dto.TimelinePointResponse;
import com.datarain.pdp.insights.dto.TrendPointResponse;
import com.datarain.pdp.signal.normalization.entity.DailyBehaviorMetric;

public final class InsightsMapper {

    private InsightsMapper() {
    }

    public static TimelinePointResponse toTimelinePoint(DailyBehaviorMetric metric) {
        return new TimelinePointResponse(
                metric.getMetricDate(),
                metric.getEnergyScore(),
                metric.getMotivationScore(),
                safeInt(metric.getFrictionCount()),
                safeInt(metric.getSocialMentionsCount()),
                safeInt(metric.getDisciplineEventsCount())
        );
    }

    public static TrendPointResponse toTrendPoint(DailyBehaviorMetric metric, Double value) {
        return new TrendPointResponse(metric.getMetricDate(), value);
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
