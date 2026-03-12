package com.datarain.pdp.testdata.service.model;

import java.time.LocalDate;
import java.util.UUID;

public record DailyBehaviorMetricsSeedResult(
        UUID userId,
        String userEmail,
        LocalDate fromDate,
        LocalDate toDate,
        int insertedCount,
        int skippedCount
) {
}
