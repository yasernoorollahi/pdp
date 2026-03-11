package com.datarain.pdp.insights.dto;

import java.time.LocalDate;

public record TrendPointResponse(
        LocalDate date,
        Double value
) {
}
