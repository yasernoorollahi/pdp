package com.datarain.pdp.insights.dto;

public record InsightSummaryResponse(
        String energy,
        String motivation,
        String friction,
        String social,
        String discipline
) {
}
