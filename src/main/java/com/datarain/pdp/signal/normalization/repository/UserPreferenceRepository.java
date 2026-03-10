package com.datarain.pdp.signal.normalization.repository;

import com.datarain.pdp.signal.normalization.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    @Query("select u.sourceHash from UserPreference u where u.sourceHash in :hashes")
    Set<String> findExistingSourceHashes(@Param("hashes") Set<String> hashes);
}
