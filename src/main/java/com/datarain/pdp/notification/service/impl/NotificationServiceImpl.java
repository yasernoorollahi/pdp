package com.datarain.pdp.notification.service.impl;

import com.datarain.pdp.notification.entity.NotificationEntity;
import com.datarain.pdp.notification.entity.NotificationStatus;
import com.datarain.pdp.notification.repository.NotificationRepository;
import com.datarain.pdp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public long markPendingAsSent() {
        List<NotificationEntity> pending = notificationRepository
                .findTop100ByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING);

        pending.forEach(notification -> notification.setStatus(NotificationStatus.SENT));
        notificationRepository.saveAll(pending);
        return pending.size();
    }
}
