package com.datarain.pdp.analytics;

import com.datarain.pdp.item.event.ItemCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BehavioralAnalyticsListener {

    @EventListener
    public void onItemCreated(ItemCreatedEvent event) {
        log.atInfo()
                .addKeyValue("event", "item.created")
                .addKeyValue("userId", event.getCreatedBy())
                .addKeyValue("itemId", event.getItemId())
                .log("Behavioral analytics event captured");
    }
}
