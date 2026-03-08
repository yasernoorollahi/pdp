package com.datarain.pdp.common.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * اضافه شد: abstract base class برای Domain Events
 */
@Getter
public abstract class AbstractDomainEvent implements DomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
}
