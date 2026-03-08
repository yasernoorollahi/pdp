package com.datarain.pdp.infrastructure.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * اضافه شد: Health indicator برای اطلاعات کلی اپلیکیشن
 * نشون میده اپ چه موقع start شده و سرویس ها OK هستند
 */
@Component("application")
public class ApplicationInfoHealthIndicator implements HealthIndicator {

    private final Instant startTime = Instant.now();

    @Override
    public Health health() {
        return Health.up()
                .withDetail("startedAt", startTime.toString())
                .withDetail("uptime", computeUptime())
                .build();
    }

    private String computeUptime() {
        long seconds = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, secs);
    }
}
