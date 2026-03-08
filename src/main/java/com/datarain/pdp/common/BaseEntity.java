package com.datarain.pdp.common;




import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;

import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    protected UUID id;

    @Version
    protected Long version;


    protected UUID tenantId;



    public UUID getId() {
        return id;
    }


    @PrePersist
    protected void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}

