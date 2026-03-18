package com.datarain.pdp.item.controller;

import com.datarain.pdp.item.dto.ItemRequest;
import com.datarain.pdp.item.dto.ItemResponse;
import com.datarain.pdp.item.entity.ItemType;
import com.datarain.pdp.item.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // اضافه شد: @PreAuthorize برای method-level security
    // اصلاح شد: hasRole → hasAuthority چون در DB مقادیر ROLE_ADMIN/ROLE_USER هستن
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ItemResponse create(@Valid @RequestBody ItemRequest request) {
        return itemService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ItemResponse getById(@PathVariable @NotNull UUID id) {
        return itemService.getById(id);
    }

    // اصلاح شد: فقط ADMIN میتونه item ها رو حذف کنه
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void delete(@PathVariable UUID id) {
        itemService.delete(id);
    }

    // اضافه شد: پارامتر search برای title search با Specification Pattern
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public Page<ItemResponse> getAll(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) String search
    ) {
        return itemService.getAll(pageable, type, search);
    }

    @PutMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void restore(@PathVariable UUID id) {
        itemService.restore(id);
    }

}
