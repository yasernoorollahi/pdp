package com.datarain.pdp.infrastructure.security.audit;

import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * اضافه شد: سرویس ثبت رویدادهای امنیتی
 * از Async استفاده میکنه تا لاگ کردن فلو اصلی رو کند نکنه
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityAuditLogRepository repository;
    private final PdpMetrics metrics;

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(SecurityEventType eventType, String email, UUID userId,
                    String ipAddress, String userAgent, String details, boolean success) {
        try {
            SecurityAuditLog entry = new SecurityAuditLog();
            entry.setEventType(eventType);
            entry.setEmail(email);
            entry.setUserId(userId);
            entry.setIpAddress(ipAddress);
            entry.setUserAgent(userAgent);
            entry.setDetails(details);
            entry.setSuccess(success);
            repository.save(entry);
        } catch (Exception e) {
            metrics.incrementAuditFailure("security");
            log.error("Failed to save security audit log for event: {}", eventType, e);
        }
    }
}
