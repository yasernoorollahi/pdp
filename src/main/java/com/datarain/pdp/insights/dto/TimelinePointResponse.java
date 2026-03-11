package com.datarain.pdp.insights.dto;

import java.time.LocalDate;

public record TimelinePointResponse(
        LocalDate date,
        Double energy,
        Double motivation,
        Integer friction,
        Integer social,
        Integer discipline
) {
}
