package com.datarain.pdp.audit;

import com.datarain.pdp.item.event.ItemCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditTrailListener {

    @EventListener
    public void onItemCreated(ItemCreatedEvent event) {
        log.info("AUDIT item created userId={} itemId={}", event.getCreatedBy(), event.getItemId());
    }
}
