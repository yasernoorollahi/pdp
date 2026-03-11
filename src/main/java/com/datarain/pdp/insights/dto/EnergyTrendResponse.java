package com.datarain.pdp.insights.dto;

import java.util.List;

public record EnergyTrendResponse(
        Double averageEnergy,
        List<TrendPointResponse> trend
) {
}
