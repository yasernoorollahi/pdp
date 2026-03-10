package com.datarain.pdp.signal.normalization.repository;

import com.datarain.pdp.signal.normalization.entity.CognitiveState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface CognitiveStateRepository extends JpaRepository<CognitiveState, UUID> {

    @Query("select c.sourceHash from CognitiveState c where c.sourceHash in :hashes")
    Set<String> findExistingSourceHashes(@Param("hashes") Set<String> hashes);
}
