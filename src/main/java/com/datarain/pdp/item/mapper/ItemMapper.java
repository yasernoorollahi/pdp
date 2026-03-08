package com.datarain.pdp.item.mapper;

import com.datarain.pdp.item.dto.ItemRequest;
import com.datarain.pdp.item.dto.ItemResponse;
import com.datarain.pdp.item.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toEntity(ItemRequest request);

    ItemResponse toResponse(Item item);
}

