package com.datarain.pdp.infrastructure.audit;

import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessEventService {

    private final BusinessEventLogRepository repository;
    private final PdpMetrics metrics;

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(BusinessEventType eventType, String email, UUID userId, String details, boolean success) {
        try {
            BusinessEventLog entry = new BusinessEventLog();
            entry.setEventType(eventType);
            entry.setEmail(email);
            entry.setUserId(userId);
            entry.setDetails(details);
            entry.setSuccess(success);
            repository.save(entry);
        } catch (Exception e) {
            metrics.incrementAuditFailure("business");
            log.error("Failed to save business event log for event: {}", eventType, e);
        }
    }
}
