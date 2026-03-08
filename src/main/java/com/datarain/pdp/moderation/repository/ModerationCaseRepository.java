package com.datarain.pdp.moderation.repository;

import com.datarain.pdp.moderation.entity.ModerationCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ModerationCaseRepository extends JpaRepository<ModerationCase, UUID>, JpaSpecificationExecutor<ModerationCase> {
}
