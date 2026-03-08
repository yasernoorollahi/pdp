package com.datarain.pdp.item.event;

import com.datarain.pdp.common.event.AbstractDomainEvent;
import com.datarain.pdp.item.entity.Item;
import lombok.Getter;

import java.util.UUID;

/**
 * اضافه شد: Domain event وقتی یه item ساخته میشه
 * سیستم‌های دیگه (مثل notification، analytics) میتونن گوش بدن
 */
@Getter
public class ItemCreatedEvent extends AbstractDomainEvent {

    private final UUID itemId;
    private final String title;
    private final UUID createdBy;

    public ItemCreatedEvent(Item item, UUID createdBy) {
        this.itemId = item.getId();
        this.title = item.getTitle();
        this.createdBy = createdBy;
    }

    @Override
    public String getEventType() {
        return "item.created";
    }
}
