package com.datarain.pdp.infrastructure.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "business_event_logs",
        indexes = {
                @Index(name = "idx_business_event_user_id", columnList = "user_id"),
                @Index(name = "idx_business_event_type", columnList = "event_type"),
                @Index(name = "idx_business_event_created_at", columnList = "created_at"),
                @Index(name = "idx_business_event_email", columnList = "email")
        })
public class BusinessEventLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private BusinessEventType eventType;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean success = true;

    @PrePersist
    protected void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
