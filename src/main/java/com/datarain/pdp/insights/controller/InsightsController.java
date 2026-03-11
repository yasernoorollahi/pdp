package com.datarain.pdp.insights.controller;

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
import com.datarain.pdp.insights.service.InsightsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping("/timeline")
    public List<TimelinePointResponse> getTimeline(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getTimeline(request, pageable);
    }

    @GetMapping("/energy")
    public EnergyTrendResponse getEnergy(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getEnergyTrend(request, pageable);
    }

    @GetMapping("/motivation")
    public MotivationTrendResponse getMotivation(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getMotivationTrend(request, pageable);
    }

    @GetMapping("/friction")
    public List<TrendPointResponse> getFriction(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 90, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getFrictionHeatmap(request, pageable);
    }

    @GetMapping("/social")
    public CountTrendResponse getSocial(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getSocialTrend(request, pageable);
    }

    @GetMapping("/discipline")
    public CountTrendResponse getDiscipline(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getDisciplineTrend(request, pageable);
    }

    @GetMapping("/summary")
    public InsightSummaryResponse getSummary(
            @Valid @ParameterObject InsightRangeRequest request
    ) {
        return insightsService.getSummary(request);
    }

    @GetMapping("/today")
    public InsightSnapshotResponse getToday() {
        return insightsService.getTodaySnapshot();
    }

    @GetMapping("/moods")
    public List<MoodWordResponse> getMoods(
            @Valid @ParameterObject InsightMoodRequest request,
            @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return insightsService.getMoodCloud(request, pageable);
    }
}
