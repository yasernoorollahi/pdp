package com.datarain.pdp.insights.dto;

public record InsightSnapshotResponse(
        Double energy,
        Double motivation,
        Integer friction,
        Integer social,
        Integer discipline
) {
}
