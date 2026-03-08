package com.datarain.pdp.infrastructure.security.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * اضافه شد: جدول audit log جداگانه برای رویدادهای امنیتی
 * مثل: login موفق/ناموفق، logout، قفل اکانت، ریست پسورد
 *
 * اصلاح شد: از BaseEntity جدا شد چون audit log نیازی به tenant_id و version نداره
 * ID خودش رو مستقیم داره - ساده و سبک
 */
@Getter
@Setter
@Entity
@Table(name = "security_audit_logs",
        indexes = {
                @Index(name = "idx_audit_user_id", columnList = "user_id"),
                @Index(name = "idx_audit_event_type", columnList = "event_type"),
                @Index(name = "idx_audit_created_at", columnList = "created_at"),
                @Index(name = "idx_audit_email", columnList = "email")
        })
public class SecurityAuditLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SecurityEventType eventType;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

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