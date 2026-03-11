package com.datarain.pdp.insights.service;

import com.datarain.pdp.insights.dto.CountTrendResponse;
import com.datarain.pdp.insights.dto.EnergyTrendResponse;
import com.datarain.pdp.insights.dto.InsightMoodRequest;
import com.datarain.pdp.insights.dto.InsightRangeRequest;
import com.datarain.pdp.insights.dto.InsightSnapshotResponse;
import com.datarain.pdp.insights.dto.InsightSummaryResponse;
import com.datarain.pdp.insights.dto.MoodWordResponse;
import com.datarain.pdp.insights.dto.MotivationTrendResponse;
import com.datarain.pdp.insights.dto.TimelinePointResponse;
import com.datarain.pdp.insights.dto.TrendPointResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InsightsService {

    List<TimelinePointResponse> getTimeline(InsightRangeRequest request, Pageable pageable);

    EnergyTrendResponse getEnergyTrend(InsightRangeRequest request, Pageable pageable);

    MotivationTrendResponse getMotivationTrend(InsightRangeRequest request, Pageable pageable);

    List<TrendPointResponse> getFrictionHeatmap(InsightRangeRequest request, Pageable pageable);

    CountTrendResponse getSocialTrend(InsightRangeRequest request, Pageable pageable);

    CountTrendResponse getDisciplineTrend(InsightRangeRequest request, Pageable pageable);

    InsightSummaryResponse getSummary(InsightRangeRequest request);

    InsightSnapshotResponse getTodaySnapshot();

    List<MoodWordResponse> getMoodCloud(InsightMoodRequest request, Pageable pageable);
}
