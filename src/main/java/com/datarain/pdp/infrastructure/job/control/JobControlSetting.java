package com.datarain.pdp.infrastructure.job.control;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_control_settings")
public class JobControlSetting {

    @Id
    @Column(name = "job_key", nullable = false, length = 100)
    private String jobKey;

    @Column(name = "enabled_override")
    private Boolean enabledOverride;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public Boolean getEnabledOverride() {
        return enabledOverride;
    }

    public void setEnabledOverride(Boolean enabledOverride) {
        this.enabledOverride = enabledOverride;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
