package com.datarain.pdp.signal.normalization.repository;

import com.datarain.pdp.signal.normalization.entity.UserTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface UserTopicRepository extends JpaRepository<UserTopic, UUID> {

    @Query("select u.sourceHash from UserTopic u where u.sourceHash in :hashes")
    Set<String> findExistingSourceHashes(@Param("hashes") Set<String> hashes);
}
