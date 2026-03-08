package com.datarain.pdp.item.entity;

import com.datarain.pdp.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "items")
public class Item extends AuditableEntity {

    @NotBlank
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.ACTIVE;


    @Column(name = "archived_at")
    protected Instant archivedAt;

}
