package com.datarain.pdp.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    // اصلاح شد: حذف @PrePersist و @PreUpdate که با JPA Auditing تداخل داشتند
    // Spring Auditing خودش این فیلدها رو پر میکنه از طریق AuditingEntityListener

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    protected Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    protected UUID createdBy;

    @LastModifiedBy
    protected UUID updatedBy;

    @Column(nullable = false)
    protected boolean enabled = true;
}
