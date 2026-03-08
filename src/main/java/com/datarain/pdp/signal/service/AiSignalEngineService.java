package com.datarain.pdp.signal.service;

import com.datarain.pdp.signal.dto.MessageSignalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AiSignalEngineService {

    long processPendingUsefulMessages(int batchSize, int maxRetries, String provider, String model);

    Page<MessageSignalResponse> getSignals(Pageable pageable, UUID userId);
}
