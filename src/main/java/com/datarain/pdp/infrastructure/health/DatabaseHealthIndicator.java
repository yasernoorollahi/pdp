package com.datarain.pdp.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * اضافه شد: Custom Health Indicator برای بررسی وضعیت کانکشن دیتابیس
 * در /actuator/health ظاهر میشه با جزئیات بیشتر
 */
@Slf4j
@Component("database")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (valid) {
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "connection pool healthy")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Connection validation failed")
                        .build();
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
