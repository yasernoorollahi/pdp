package com.datarain.pdp.extraction.mapper;

import com.datarain.pdp.extraction.dto.ExtractionResponse;
import com.datarain.pdp.extraction.repository.dto.AiExtractionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExtractionMapper {

    ExtractionResponse toResponse(AiExtractionResponse source);
}
