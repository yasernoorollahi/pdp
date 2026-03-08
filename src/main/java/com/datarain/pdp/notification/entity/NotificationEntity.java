package com.datarain.pdp.notification.entity;

import com.datarain.pdp.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_item_id", columnList = "item_id"),
                @Index(name = "idx_notifications_user_id", columnList = "user_id"),
                @Index(name = "idx_notifications_status", columnList = "status")
        })
public class NotificationEntity extends AuditableEntity {

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;
}
