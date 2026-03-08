package com.datarain.pdp.moderation.service;

import com.datarain.pdp.moderation.dto.ModerationCaseActionRequest;
import com.datarain.pdp.moderation.dto.ModerationCaseCreateRequest;
import com.datarain.pdp.moderation.dto.ModerationCaseResponse;
import com.datarain.pdp.moderation.entity.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ModerationCaseService {

    Page<ModerationCaseResponse> findAll(Pageable pageable, ModerationStatus status);

    ModerationCaseResponse getById(UUID id);

    ModerationCaseResponse createCase(ModerationCaseCreateRequest request);

    ModerationCaseResponse approve(UUID id, ModerationCaseActionRequest request);

    ModerationCaseResponse reject(UUID id, ModerationCaseActionRequest request);

    ModerationCaseResponse autoBlock(UUID id, ModerationCaseActionRequest request);
}
