package com.datarain.pdp.admin.service;

import com.datarain.pdp.admin.dto.SystemOverviewResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
public class SystemOverviewServiceImpl implements SystemOverviewService {

    private final HealthEndpoint healthEndpoint;
    private final MeterRegistry meterRegistry;

    @Override
    public SystemOverviewResponse getOverview() {
        HealthComponent health = healthEndpoint.health();
        Map<String, String> components = extractComponentStatuses(health);

        SystemOverviewResponse.MetricSnapshot metrics = new SystemOverviewResponse.MetricSnapshot(
                readGauge("jvm.memory.used", "area", "heap"),
                readGauge("jvm.memory.max", "area", "heap"),
                readGauge("jvm.threads.live"),
                readGauge("process.cpu.usage"),
                readGauge("system.cpu.usage"),
                readGauge("process.uptime"),
                readTimerCount("http.server.requests"),
                readTimerMeanSeconds("http.server.requests"),
                new SystemOverviewResponse.HikariSnapshot(
                        readGauge("hikaricp.connections.active"),
                        readGauge("hikaricp.connections.idle"),
                        readGauge("hikaricp.connections.pending"),
                        readGauge("hikaricp.connections.max"),
                        readGauge("hikaricp.connections.min")
                ),
                readCounter("pdp.item.created")
        );

        return new SystemOverviewResponse(
                resolveStatus(health),
                components,
                metrics,
                Instant.now()
        );
    }

    private Map<String, String> extractComponentStatuses(HealthComponent health) {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (health instanceof SystemHealth systemHealth) {
            systemHealth.getComponents().forEach((name, component) ->
                    statuses.put(name, resolveStatus(component))
            );
        }
        return statuses;
    }

    private String resolveStatus(HealthComponent health) {
        if (health instanceof org.springframework.boot.actuate.health.Health h) {
            return h.getStatus().getCode();
        }
        if (health instanceof SystemHealth systemHealth) {
            Status status = systemHealth.getStatus();
            return status != null ? status.getCode() : Status.UNKNOWN.getCode();
        }
        return Status.UNKNOWN.getCode();
    }

    private double readGauge(String meterName) {
        Gauge gauge = meterRegistry.find(meterName).gauge();
        if (gauge != null) {
            return gauge.value();
        }

        Collection<Gauge> gauges = meterRegistry.find(meterName).gauges();
        if (!gauges.isEmpty()) {
            return gauges.iterator().next().value();
        }

        Meter meter = meterRegistry.find(meterName).meter();
        if (meter != null) {
            Iterator<Measurement> iterator = meter.measure().iterator();
            return iterator.hasNext() ? iterator.next().getValue() : 0.0d;
        }
        return 0.0d;
    }

    private double readGauge(String meterName, String tagKey, String tagValue) {
        Gauge gauge = meterRegistry.find(meterName).tag(tagKey, tagValue).gauge();
        return gauge != null ? gauge.value() : 0.0d;
    }

    private double readCounter(String meterName) {
        Counter counter = meterRegistry.find(meterName).counter();
        return counter != null ? counter.count() : 0.0d;
    }

    private double readTimerCount(String meterName) {
        Timer timer = meterRegistry.find(meterName).timer();
        return timer != null ? timer.count() : 0.0d;
    }

    private double readTimerMeanSeconds(String meterName) {
        Timer timer = meterRegistry.find(meterName).timer();
        return timer != null ? timer.mean(java.util.concurrent.TimeUnit.SECONDS) : 0.0d;
    }
}
