package com.datarain.pdp.signal.normalization.dto;

import java.time.LocalDate;

public record ActivitySignal(
        String name,
        LocalDate date
) {
}
