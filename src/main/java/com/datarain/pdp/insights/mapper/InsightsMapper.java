package com.datarain.pdp.insights.mapper;

import com.datarain.pdp.insights.dto.TimelinePointResponse;
import com.datarain.pdp.insights.dto.TrendPointResponse;
import com.datarain.pdp.signal.service.model.DailyBehaviorMetricView;

public final class InsightsMapper {

    private InsightsMapper() {
    }

    public static TimelinePointResponse toTimelinePoint(DailyBehaviorMetricView metric) {
        return new TimelinePointResponse(
                metric.metricDate(),
                metric.energyScore(),
                metric.motivationScore(),
                safeInt(metric.frictionCount()),
                safeInt(metric.socialMentionsCount()),
                safeInt(metric.disciplineEventsCount())
        );
    }

    public static TrendPointResponse toTrendPoint(DailyBehaviorMetricView metric, Double value) {
        return new TrendPointResponse(metric.metricDate(), value);
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
