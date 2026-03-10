package com.datarain.pdp.signal.normalization.repository;

import com.datarain.pdp.signal.normalization.entity.ToneState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface ToneStateRepository extends JpaRepository<ToneState, UUID> {

    @Query("select t.sourceHash from ToneState t where t.sourceHash in :hashes")
    Set<String> findExistingSourceHashes(@Param("hashes") Set<String> hashes);
}
