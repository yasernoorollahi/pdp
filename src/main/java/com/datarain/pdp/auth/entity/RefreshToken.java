package com.datarain.pdp.auth.entity;

import com.datarain.pdp.common.AuditableEntity;
import com.datarain.pdp.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends AuditableEntity {

//    @Id
//    @Column(nullable = false, updatable = false)
//    private UUID id;

//    @Column(nullable = false, unique = true, length = 512)
//    private String token;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;



    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false)
    private String device;

    @Column(nullable = false)
    private String ipAddress;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

//    @PrePersist
//    public void prePersist() {
//        this.id = UUID.randomUUID();
//    }
}

