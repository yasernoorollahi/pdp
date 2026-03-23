package com.datarain.pdp.insights.service.impl;

import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.audit.BusinessEventService;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
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
import com.datarain.pdp.insights.mapper.InsightsMapper;
import com.datarain.pdp.insights.service.InsightsService;
import com.datarain.pdp.signal.service.SignalQueryService;
import com.datarain.pdp.signal.service.model.DailyBehaviorMetricSummaryView;
import com.datarain.pdp.signal.service.model.DailyBehaviorMetricView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightsServiceImpl implements InsightsService {

    private static final int DEFAULT_DAYS = 15;
    private static final int DEFAULT_SUMMARY_DAYS = 7;
    private static final int DEFAULT_MOOD_DAYS = 30;
    private static final int DEFAULT_MOOD_LIMIT = 50;

    private final SignalQueryService signalQueryService;
    private final BusinessEventService businessEventService;
    private final PdpMetrics metrics;

    @Override
    @Transactional(readOnly = true)
    public List<TimelinePointResponse> getTimeline(InsightRangeRequest request, Pageable pageable) {
        int days = request.resolveDays(DEFAULT_DAYS);
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.timeline.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("traceId", traceId)
                .log("Insights timeline requested");

        metrics.incrementInsights("timeline");
        audit("timeline", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        Page<DailyBehaviorMetricView> page = signalQueryService.findMetrics(userId, fromDate, pageable);

        return page.stream()
                .map(InsightsMapper::toTimelinePoint)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnergyTrendResponse getEnergyTrend(InsightRangeRequest request, Pageable pageable) {
        int days = request.resolveDays(DEFAULT_DAYS);
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.energy.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("traceId", traceId)
                .log("Insights energy trend requested");

        metrics.incrementInsights("energy");
        audit("energy", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        Page<DailyBehaviorMetricView> page = signalQueryService.findMetrics(userId, fromDate, pageable);

        List<TrendPointResponse> trend = page.stream()
                .map(metric -> InsightsMapper.toTrendPoint(metric, metric.energyScore()))
                .toList();

        Double average = summarize(userId, fromDate).avgEnergy();
        return new EnergyTrendResponse(average, trend);
    }

    @Override
    @Transactional(readOnly = true)
    public MotivationTrendResponse getMotivationTrend(InsightRangeRequest request, Pageable pageable) {
        int days = request.resolveDays(DEFAULT_DAYS);
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.motivation.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("traceId", traceId)
                .log("Insights motivation trend requested");

        metrics.incrementInsights("motivation");
        audit("motivation", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        Page<DailyBehaviorMetricView> page = signalQueryService.findMetrics(userId, fromDate, pageable);

        List<TrendPointResponse> trend = page.stream()
                .map(metric -> InsightsMapper.toTrendPoint(metric, metric.motivationScore()))
                .toList();

        Double average = summarize(userId, fromDate).avgMotivation();
        return new MotivationTrendResponse(average, trend);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendPointResponse> getFrictionHeatmap(InsightRangeRequest request, Pageable pageable) {
        int days = request.resolveDays(DEFAULT_DAYS);
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.friction.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("traceId", traceId)
                .log("Insights friction heatmap requested");

        metrics.incrementInsights("friction");
        audit("friction", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        Page<DailyBehaviorMetricView> page = signalQueryService.findMetrics(userId, fromDate, pageable);

        return page.stream()
                .map(metric -> InsightsMapper.toTrendPoint(metric, (double) safeInt(metric.frictionCount())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CountTrendResponse getSocialTrend(InsightRangeRequest request, Pageable pageable) {
        int days = request.resolveDays(DEFAULT_DAYS);
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.social.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("traceId", traceId)
                .log("Insights social trend requested");

        metrics.incrementInsights("social");
        audit("social", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        Page<DailyBehaviorMetricView> page = signalQueryService.findMetrics(userId, fromDate, pageable);

        List<TrendPointResponse> trend = page.stream()
                .map(metric -> InsightsMapper.toTrendPoint(metric, (double) safeInt(metric.socialMentionsCount())))
                .toList();

        int total = summarize(userId, fromDate).socialSum().intValue();
        return new CountTrendResponse(total, trend);
    }

    @Override
    @Transactional(readOnly = true)
    public CountTrendResponse getDisciplineTrend(InsightRangeRequest request, Pageable pageable) {
        int days = request.resolveDays(DEFAULT_DAYS);
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.discipline.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("traceId", traceId)
                .log("Insights discipline trend requested");

        metrics.incrementInsights("discipline");
        audit("discipline", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        Page<DailyBehaviorMetricView> page = signalQueryService.findMetrics(userId, fromDate, pageable);

        List<TrendPointResponse> trend = page.stream()
                .map(metric -> InsightsMapper.toTrendPoint(metric, (double) safeInt(metric.disciplineEventsCount())))
                .toList();

        int total = summarize(userId, fromDate).disciplineSum().intValue();
        return new CountTrendResponse(total, trend);
    }

    @Override
    @Transactional(readOnly = true)
    public InsightSummaryResponse getSummary(InsightRangeRequest request) {
        int days = request.resolveDays(DEFAULT_SUMMARY_DAYS);
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.summary.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("traceId", traceId)
                .log("Insights summary requested");

        metrics.incrementInsights("summary");
        audit("summary", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        DailyBehaviorMetricSummaryView summary = summarize(userId, fromDate);

        String energyLevel = scoreToLevel(summary.avgEnergy(), "stable");
        String motivationLevel = scoreToLevel(summary.avgMotivation(), "high");
        String frictionLevel = countToLevel(summary.frictionSum(), "moderate", 1, 3);
        String socialLevel = countToLevel(summary.socialSum(), "low", 1, 4);
        String disciplineLevel = countToLevel(summary.disciplineSum(), "good", 2, 5);

        return new InsightSummaryResponse(energyLevel, motivationLevel, frictionLevel, socialLevel, disciplineLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public InsightSnapshotResponse getTodaySnapshot() {
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.today.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("traceId", traceId)
                .log("Insights today snapshot requested");

        metrics.incrementInsights("today");
        audit("today", userId, null);

        return signalQueryService.findLatestMetric(userId)
                .map(metric -> new InsightSnapshotResponse(
                        metric.energyScore(),
                        metric.motivationScore(),
                        safeInt(metric.frictionCount()),
                        safeInt(metric.socialMentionsCount()),
                        safeInt(metric.disciplineEventsCount())
                ))
                .orElseGet(() -> new InsightSnapshotResponse(0.0, 0.0, 0, 0, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoodWordResponse> getMoodCloud(InsightMoodRequest request, Pageable pageable) {
        int days = request.resolveDays(DEFAULT_MOOD_DAYS);
        int limit = request.resolveLimit(DEFAULT_MOOD_LIMIT);
        if (pageable != null && pageable.getPageSize() > 0) {
            limit = Math.min(limit, pageable.getPageSize());
        }
        UUID userId = SecurityUtils.currentUserId();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "insights.moods.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("days", days)
                .addKeyValue("limit", limit)
                .addKeyValue("traceId", traceId)
                .log("Insights mood cloud requested");

        metrics.incrementInsights("moods");
        audit("moods", userId, days);

        LocalDate fromDate = LocalDate.now(ZoneOffset.UTC).minusDays(days);
        List<String> moods = signalQueryService.findMoodSummaries(userId, fromDate);

        Map<String, Integer> counts = new HashMap<>();
        for (String mood : moods) {
            if (mood == null || mood.isBlank()) {
                continue;
            }
            String[] tokens = mood.toLowerCase(Locale.ROOT).split("[^\\p{L}]+");
            for (String token : tokens) {
                if (token.isBlank() || token.length() < 2) {
                    continue;
                }
                counts.merge(token, 1, Integer::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(entry -> new MoodWordResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private DailyBehaviorMetricSummaryView summarize(UUID userId, LocalDate fromDate) {
        return signalQueryService.summarize(userId, fromDate);
    }

    private String scoreToLevel(Double score, String defaultLevel) {
        if (score == null) {
            return defaultLevel;
        }
        if (score >= 0.75) {
            return "high";
        }
        if (score >= 0.55) {
            return "stable";
        }
        if (score >= 0.35) {
            return "low";
        }
        return "very_low";
    }

    private String countToLevel(Long total, String defaultLevel, long lowThreshold, long highThreshold) {
        if (total == null) {
            return defaultLevel;
        }
        if (total <= lowThreshold) {
            return "low";
        }
        if (total <= highThreshold) {
            return "moderate";
        }
        return "high";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void audit(String endpoint, UUID userId, Integer days) {
        String email = SecurityUtils.currentUsername();
        String details = days == null
                ? "insights." + endpoint
                : "insights." + endpoint + " days=" + days;
        businessEventService.log(BusinessEventType.INSIGHTS_VIEWED, email, userId, details, true);
    }

}
