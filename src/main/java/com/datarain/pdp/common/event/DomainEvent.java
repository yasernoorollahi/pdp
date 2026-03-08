package com.datarain.pdp.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * اضافه شد: base interface برای Domain Events داخل monolith
 * هر domain event باید این رو implement کنه
 */
public interface DomainEvent {
    UUID getEventId();
    Instant getOccurredAt();
    String getEventType();
}
