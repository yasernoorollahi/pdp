package com.datarain.pdp.signal.mapper;

import com.datarain.pdp.signal.dto.MessageSignalResponse;
import com.datarain.pdp.signal.entity.MessageSignal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageSignalMapper {

    MessageSignalResponse toResponse(MessageSignal entity);
}
