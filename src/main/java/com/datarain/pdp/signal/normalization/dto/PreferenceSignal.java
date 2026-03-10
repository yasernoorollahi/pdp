package com.datarain.pdp.signal.normalization.dto;

import com.datarain.pdp.signal.normalization.entity.PreferenceType;

public record PreferenceSignal(
        PreferenceType preferenceType,
        String value
) {
}
