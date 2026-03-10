package com.datarain.pdp.signal.normalization.repository;

import com.datarain.pdp.signal.normalization.entity.IntentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface IntentItemRepository extends JpaRepository<IntentItem, UUID> {

    @Query("select i.sourceHash from IntentItem i where i.sourceHash in :hashes")
    Set<String> findExistingSourceHashes(@Param("hashes") Set<String> hashes);
}
