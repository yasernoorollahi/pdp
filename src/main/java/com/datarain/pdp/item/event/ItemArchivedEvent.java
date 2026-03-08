package com.datarain.pdp.item.event;

import com.datarain.pdp.common.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * اضافه شد: Domain event وقتی item آرشیو میشه
 */
@Getter
public class ItemArchivedEvent extends AbstractDomainEvent {

    private final UUID itemId;

    public ItemArchivedEvent(UUID itemId) {
        this.itemId = itemId;
    }

    @Override
    public String getEventType() {
        return "item.archived";
    }
}
