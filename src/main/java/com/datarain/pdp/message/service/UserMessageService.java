package com.datarain.pdp.message.service;

import com.datarain.pdp.message.dto.UserMessageCreateRequest;
import com.datarain.pdp.message.dto.UserMessageProcessedRequest;
import com.datarain.pdp.message.dto.UserMessageResponse;
import com.datarain.pdp.message.dto.UserMessageUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface UserMessageService {

    UserMessageResponse create(UserMessageCreateRequest request);

    UserMessageResponse getById(UUID id);

    UserMessageResponse update(UUID id, UserMessageUpdateRequest request);

    UserMessageResponse setProcessed(UUID id, UserMessageProcessedRequest request);

    void delete(UUID id);

    Page<UserMessageResponse> getAll(Pageable pageable, Boolean processed, LocalDate fromDate, LocalDate toDate);

    long analyzePendingMessages(int batchSize, String provider, String model);
}
