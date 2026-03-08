package com.datarain.pdp.item.repository;

import com.datarain.pdp.item.entity.Item;
import com.datarain.pdp.item.entity.ItemStatus;
import com.datarain.pdp.item.entity.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// اضافه شد: JpaSpecificationExecutor برای Specification Pattern + countByStatus
public interface ItemRepository extends JpaRepository<Item, UUID>, JpaSpecificationExecutor<Item> {

    Page<Item> findByType(ItemType type, Pageable pageable);

    Page<Item> findByTypeNot(ItemType type, Pageable pageable);

    Page<Item> findByStatus(ItemStatus status, Pageable pageable);

    Page<Item> findByTypeAndStatus(ItemType type, ItemStatus status, Pageable pageable);

    List<Item> findByStatusAndArchivedAtBefore(ItemStatus status, Instant time);

    // اضافه شد: برای admin stats
    long countByStatus(ItemStatus status);
}
