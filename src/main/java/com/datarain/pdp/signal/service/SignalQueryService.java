package com.datarain.pdp.signal.service;

import com.datarain.pdp.signal.service.model.DailyBehaviorMetricSummaryView;
import com.datarain.pdp.signal.service.model.DailyBehaviorMetricView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SignalQueryService {

    Page<DailyBehaviorMetricView> findMetrics(UUID userId, LocalDate fromDate, Pageable pageable);

    Optional<DailyBehaviorMetricView> findLatestMetric(UUID userId);

    DailyBehaviorMetricSummaryView summarize(UUID userId, LocalDate fromDate);

    List<String> findMoodSummaries(UUID userId, LocalDate fromDate);
}
