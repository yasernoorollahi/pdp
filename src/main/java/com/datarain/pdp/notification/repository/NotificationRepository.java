package com.datarain.pdp.notification.repository;

import com.datarain.pdp.notification.entity.NotificationEntity;
import com.datarain.pdp.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findTop100ByStatusOrderByCreatedAtAsc(NotificationStatus status);
    long countByStatus(NotificationStatus status);
}
