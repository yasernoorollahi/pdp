package com.datarain.pdp.infrastructure.job.control;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobControlSettingRepository extends JpaRepository<JobControlSetting, String> {
    Optional<JobControlSetting> findTopByOrderByUpdatedAtDesc();
}
