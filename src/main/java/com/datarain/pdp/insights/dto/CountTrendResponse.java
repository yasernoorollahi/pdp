package com.datarain.pdp.insights.dto;

import java.util.List;

public record CountTrendResponse(
        Integer total,
        List<TrendPointResponse> trend
) {
}
