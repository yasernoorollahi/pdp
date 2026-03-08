package com.datarain.pdp.moderation.mapper;

import com.datarain.pdp.moderation.dto.ModerationCaseCreateRequest;
import com.datarain.pdp.moderation.dto.ModerationCaseResponse;
import com.datarain.pdp.moderation.entity.ModerationCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModerationCaseMapper {

    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    ModerationCase toEntity(ModerationCaseCreateRequest request);

    ModerationCaseResponse toResponse(ModerationCase entity);
}
