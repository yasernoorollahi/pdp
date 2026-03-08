package com.datarain.pdp.item.service.impl;

import com.datarain.pdp.item.dto.ItemRequest;
import com.datarain.pdp.item.dto.ItemResponse;
import com.datarain.pdp.item.entity.Item;
import com.datarain.pdp.item.entity.ItemStatus;
import com.datarain.pdp.item.entity.ItemType;
import com.datarain.pdp.exception.business.ItemNotFoundException;
import com.datarain.pdp.item.event.ItemArchivedEvent;
import com.datarain.pdp.item.event.ItemCreatedEvent;
import com.datarain.pdp.item.mapper.ItemMapper;
import com.datarain.pdp.item.repository.ItemRepository;
import com.datarain.pdp.item.service.ItemService;
import com.datarain.pdp.item.specification.ItemSpecification;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final PdpMetrics metrics;
    // اضافه شد: برای publish کردن domain events
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ItemResponse create(ItemRequest request) {
        Instant startedAt = Instant.now();
        Item item = itemMapper.toEntity(request);
        Item saved = itemRepository.save(item);

        UUID currentUserId = SecurityUtils.currentUserId();
        eventPublisher.publishEvent(new ItemCreatedEvent(saved, currentUserId));
        metrics.getItemCreatedCounter().increment();
        metrics.getItemCreateTimer().record(Duration.between(startedAt, Instant.now()));

        log.info("Item created: {}", saved.getId());
        return itemMapper.toResponse(saved);
    }

    @Override
    public ItemResponse getById(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        return itemMapper.toResponse(item);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        item.setStatus(ItemStatus.ARCHIVED);
        item.setArchivedAt(Instant.now());
        itemRepository.save(item);

        // اضافه شد: publish domain event
        eventPublisher.publishEvent(new ItemArchivedEvent(id));
        metrics.getItemArchivedCounter().increment();
        log.info("Item archived: {}", id);
    }

    // اصلاح شد: استفاده از Specification Pattern + پشتیبانی از search
    @Override
    public Page<ItemResponse> getAll(Pageable pageable, ItemType type, String search) {
        Specification<Item> spec = ItemSpecification.hasStatus(ItemStatus.ACTIVE)
                .and(ItemSpecification.isEnabled())
                .and(ItemSpecification.hasType(type))
                .and(ItemSpecification.titleContains(search));

        return itemRepository.findAll(spec, pageable)
                .map(itemMapper::toResponse);
    }

    @Override
    @Transactional
    public void restore(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        item.setStatus(ItemStatus.ACTIVE);
        item.setArchivedAt(null);
        itemRepository.save(item);
        log.info("Item restored: {}", id);
    }
}
