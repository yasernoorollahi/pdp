package com.datarain.pdp.signal.service.impl;

import com.datarain.pdp.signal.normalization.entity.DailyBehaviorMetric;
import com.datarain.pdp.signal.normalization.repository.DailyBehaviorMetricRepository;
import com.datarain.pdp.signal.normalization.repository.DailyBehaviorMetricSummaryProjection;
import com.datarain.pdp.signal.service.SignalQueryService;
import com.datarain.pdp.signal.service.model.DailyBehaviorMetricSummaryView;
import com.datarain.pdp.signal.service.model.DailyBehaviorMetricView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignalQueryServiceImpl implements SignalQueryService {

    private final DailyBehaviorMetricRepository dailyBehaviorMetricRepository;

    @Override
    public Page<DailyBehaviorMetricView> findMetrics(UUID userId, LocalDate fromDate, Pageable pageable) {
        return dailyBehaviorMetricRepository
                .findByUserIdAndMetricDateGreaterThanEqualOrderByMetricDateAsc(userId, fromDate, pageable)
                .map(SignalQueryServiceImpl::toView);
    }

    @Override
    public Optional<DailyBehaviorMetricView> findLatestMetric(UUID userId) {
        return dailyBehaviorMetricRepository.findTopByUserIdOrderByMetricDateDesc(userId)
                .map(SignalQueryServiceImpl::toView);
    }

    @Override
    public DailyBehaviorMetricSummaryView summarize(UUID userId, LocalDate fromDate) {
        DailyBehaviorMetricSummaryProjection summary = dailyBehaviorMetricRepository.summarize(userId, fromDate);
        if (summary == null) {
            return DailyBehaviorMetricSummaryView.empty();
        }
        return new DailyBehaviorMetricSummaryView(
                summary.getAvgEnergy(),
                summary.getAvgMotivation(),
                summary.getFrictionSum(),
                summary.getSocialSum(),
                summary.getDisciplineSum()
        );
    }

    @Override
    public List<String> findMoodSummaries(UUID userId, LocalDate fromDate) {
        return dailyBehaviorMetricRepository.findMoodSummaries(userId, fromDate);
    }

    private static DailyBehaviorMetricView toView(DailyBehaviorMetric metric) {
        return new DailyBehaviorMetricView(
                metric.getMetricDate(),
                metric.getEnergyScore(),
                metric.getMotivationScore(),
                metric.getFrictionCount(),
                metric.getSocialMentionsCount(),
                metric.getDisciplineEventsCount()
        );
    }
}
