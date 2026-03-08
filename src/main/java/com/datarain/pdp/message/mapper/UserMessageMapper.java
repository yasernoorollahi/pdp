package com.datarain.pdp.message.mapper;

import com.datarain.pdp.message.dto.UserMessageCreateRequest;
import com.datarain.pdp.message.dto.UserMessageResponse;
import com.datarain.pdp.message.entity.UserMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "processed", ignore = true)
    @Mapping(target = "analysisStatus", ignore = true)
    @Mapping(target = "signalScore", ignore = true)
    @Mapping(target = "signalDecision", ignore = true)
    @Mapping(target = "signalReason", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "processingStatus", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    @Mapping(target = "processingStartedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    UserMessage toEntity(UserMessageCreateRequest request);

    UserMessageResponse toResponse(UserMessage entity);
}
