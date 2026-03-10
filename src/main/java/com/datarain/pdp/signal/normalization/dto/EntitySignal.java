package com.datarain.pdp.signal.normalization.dto;

import com.datarain.pdp.signal.normalization.entity.UserEntityType;

public record EntitySignal(
        String name,
        String canonicalName,
        UserEntityType entityType,
        Double confidence
) {
}
