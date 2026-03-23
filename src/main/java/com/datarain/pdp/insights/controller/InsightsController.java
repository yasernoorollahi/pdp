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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Insights", description = "User insight timelines, trends, and summaries.")
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping("/timeline")
    @Operation(summary = "Get the user's insight timeline over a date range.")
    public List<TimelinePointResponse> getTimeline(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getTimeline(request, pageable);
    }

    @GetMapping("/energy")
    @Operation(summary = "Get the user's energy trend over time.")
    public EnergyTrendResponse getEnergy(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getEnergyTrend(request, pageable);
    }

    @GetMapping("/motivation")
    @Operation(summary = "Get the user's motivation trend over time.")
    public MotivationTrendResponse getMotivation(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getMotivationTrend(request, pageable);
    }

    @GetMapping("/friction")
    @Operation(summary = "Get the user's friction heatmap over time.")
    public List<TrendPointResponse> getFriction(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 90, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getFrictionHeatmap(request, pageable);
    }

    @GetMapping("/social")
    @Operation(summary = "Get the user's social activity trend over time.")
    public CountTrendResponse getSocial(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getSocialTrend(request, pageable);
    }

    @GetMapping("/discipline")
    @Operation(summary = "Get the user's discipline trend over time.")
    public CountTrendResponse getDiscipline(
            @Valid @ParameterObject InsightRangeRequest request,
            @ParameterObject @PageableDefault(size = 60, sort = "metricDate") Pageable pageable
    ) {
        return insightsService.getDisciplineTrend(request, pageable);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get an insight summary for the requested period.")
    public InsightSummaryResponse getSummary(
            @Valid @ParameterObject InsightRangeRequest request
    ) {
        return insightsService.getSummary(request);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's snapshot of key insight metrics.")
    public InsightSnapshotResponse getToday() {
        return insightsService.getTodaySnapshot();
    }

    @GetMapping("/moods")
    @Operation(summary = "Get a mood word cloud derived from recent messages.")
    public List<MoodWordResponse> getMoods(
            @Valid @ParameterObject InsightMoodRequest request,
            @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return insightsService.getMoodCloud(request, pageable);
    }
}
