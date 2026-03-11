package com.datarain.pdp.insights.dto;

import java.util.List;

public record MotivationTrendResponse(
        Double averageMotivation,
        List<TrendPointResponse> trend
) {
}
