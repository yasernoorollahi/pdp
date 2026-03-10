package com.datarain.pdp.signal.normalization.repository;

import com.datarain.pdp.signal.normalization.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface UserProjectRepository extends JpaRepository<UserProject, UUID> {

    @Query("select u.sourceHash from UserProject u where u.sourceHash in :hashes")
    Set<String> findExistingSourceHashes(@Param("hashes") Set<String> hashes);
}
