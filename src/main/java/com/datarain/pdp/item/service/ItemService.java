package com.datarain.pdp.item.service;

import com.datarain.pdp.item.dto.ItemRequest;
import com.datarain.pdp.item.dto.ItemResponse;
import com.datarain.pdp.item.entity.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ItemService {

    ItemResponse create(ItemRequest request);

    ItemResponse getById(UUID id);

    void delete(UUID id);

    // اصلاح شد: اضافه کردن search parameter
    Page<ItemResponse> getAll(Pageable pageable, ItemType type, String search);

    // نگه داشتن backward compat با overload
    default Page<ItemResponse> getAll(Pageable pageable, ItemType type) {
        return getAll(pageable, type, null);
    }

    void restore(UUID id);
}
