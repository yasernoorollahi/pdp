package com.datarain.pdp.notification;

import com.datarain.pdp.item.event.ItemCreatedEvent;
import com.datarain.pdp.notification.entity.NotificationEntity;
import com.datarain.pdp.notification.entity.NotificationStatus;
import com.datarain.pdp.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPreparationListener {

    private final NotificationRepository notificationRepository;

    @EventListener
    public void onItemCreated(ItemCreatedEvent event) {
        NotificationEntity notification = new NotificationEntity();
        notification.setItemId(event.getItemId());
        notification.setUserId(event.getCreatedBy());
        notification.setStatus(NotificationStatus.PENDING);
        notificationRepository.save(notification);

        log.info("Notification prepared with PENDING status for itemId={}", event.getItemId());
    }
}
