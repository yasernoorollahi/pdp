package com.datarain.pdp.signal.normalization.service;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;

public interface MetricsAggregationService {

    void aggregate(MessageSignal signal, ParsedSignal parsedSignal);
}
