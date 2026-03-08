package com.datarain.pdp.signal.service;

import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.signal.dto.CombinedSignalData;

public interface SignalOrchestrator {

    CombinedSignalData orchestrate(ExtractionRequest request);
}
