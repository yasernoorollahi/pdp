package com.datarain.pdp.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * اضافه شد: Custom Metrics با Micrometer
 * این metrics از /actuator/prometheus در دسترسه
 * میشه توی Grafana dashboard کرد
 */
@Component
public class PdpMetrics {

    // counters
    @Getter private final Counter loginSuccessCounter;
    @Getter private final Counter loginFailedCounter;
    @Getter private final Counter rateLimitHitCounter;
    @Getter private final Counter tokenRefreshCounter;
    @Getter private final Counter extractionRequestedCounter;
    @Getter private final Counter extractionFailedCounter;
    @Getter private final Counter userMessageCreatedCounter;
    @Getter private final Counter userMessageUpdatedCounter;
    @Getter private final Counter userMessageDeletedCounter;
    @Getter private final Counter userMessageProcessedCounter;
    @Getter private final Counter signalEngineSuccessCounter;
    @Getter private final Counter signalEngineFailureCounter;
    @Getter private final Counter signalEngineSignalsStoredCounter;
    @Getter private final Counter signalNormalizationSuccessCounter;
    @Getter private final Counter signalNormalizationFailureCounter;
    @Getter private final Counter signalNormalizationSignalsNormalizedCounter;
    @Getter private final Counter testDataDailyBehaviorSeedCounter;
    @Getter private final Counter adminSystemOverviewCounter;
    @Getter private final Counter adminJobControlViewedCounter;
    @Getter private final Counter adminJobControlUpdatedCounter;

    // timers - برای اندازه‌گیری زمان عملیات‌های مهم
    @Getter private final Timer authLoginTimer;

    private final MeterRegistry registry;

    public PdpMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.loginSuccessCounter = Counter.builder("pdp.auth.login.success")
                .description("تعداد ورود موفق")
                .register(registry);

        this.loginFailedCounter = Counter.builder("pdp.auth.login.failed")
                .description("تعداد ورود ناموفق")
                .register(registry);

        this.rateLimitHitCounter = Counter.builder("pdp.rate_limit.hit")
                .description("تعداد دفعاتی که rate limit رد شد")
                .register(registry);

        this.tokenRefreshCounter = Counter.builder("pdp.auth.token.refresh")
                .description("تعداد دفعات refresh token")
                .register(registry);

        this.extractionRequestedCounter = Counter.builder("pdp.extraction.requested")
                .description("تعداد درخواست های extraction")
                .register(registry);

        this.extractionFailedCounter = Counter.builder("pdp.extraction.failed")
                .description("تعداد درخواست های extraction ناموفق")
                .register(registry);

        this.userMessageCreatedCounter = Counter.builder("pdp.user_message.created")
                .description("تعداد پیام های کاربر ایجاد شده")
                .register(registry);

        this.userMessageUpdatedCounter = Counter.builder("pdp.user_message.updated")
                .description("تعداد پیام های کاربر ویرایش شده")
                .register(registry);

        this.userMessageDeletedCounter = Counter.builder("pdp.user_message.deleted")
                .description("تعداد پیام های کاربر حذف شده")
                .register(registry);

        this.userMessageProcessedCounter = Counter.builder("pdp.user_message.processed")
                .description("تعداد پیام های کاربر که processed شدند")
                .register(registry);

        this.signalEngineSuccessCounter = Counter.builder("pdp.signal_engine.success")
                .description("تعداد پردازش موفق در AI signal engine")
                .register(registry);

        this.signalEngineFailureCounter = Counter.builder("pdp.signal_engine.failure")
                .description("تعداد پردازش ناموفق در AI signal engine")
                .register(registry);

        this.signalEngineSignalsStoredCounter = Counter.builder("pdp.signal_engine.signals_stored")
                .description("تعداد snapshot های signals ذخیره شده")
                .register(registry);

        this.signalNormalizationSuccessCounter = Counter.builder("pdp.signal_normalization.success")
                .description("تعداد نرمال سازی موفق")
                .register(registry);

        this.signalNormalizationFailureCounter = Counter.builder("pdp.signal_normalization.failure")
                .description("تعداد نرمال سازی ناموفق")
                .register(registry);

        this.signalNormalizationSignalsNormalizedCounter = Counter.builder("pdp.signal_normalization.signals_normalized")
                .description("تعداد signals نرمال شده")
                .register(registry);

        this.testDataDailyBehaviorSeedCounter = Counter.builder("pdp.test_data.daily_behavior.seeded")
                .description("تعداد اجرای seeding برای daily behavior metrics")
                .register(registry);

        this.adminSystemOverviewCounter = Counter.builder("pdp.admin.system_overview")
                .description("تعداد درخواست های admin system overview")
                .register(registry);

        this.adminJobControlViewedCounter = Counter.builder("pdp.admin.job_control.viewed")
                .description("تعداد درخواست های مشاهده تنظیمات job")
                .register(registry);

        this.adminJobControlUpdatedCounter = Counter.builder("pdp.admin.job_control.updated")
                .description("تعداد تغییرات در تنظیمات job")
                .register(registry);

        this.authLoginTimer = Timer.builder("pdp.auth.login.duration")
                .description("زمان پردازش login")
                .register(registry);
    }

    public void incrementInsights(String type) {
        registry.counter("pdp.insights.request", "type", type).increment();
    }

    public void incrementRateLimitRejected(String path) {
        rateLimitHitCounter.increment();
        registry.counter("pdp.rate_limit.rejected", "path", path).increment();
    }

    public void incrementAuditFailure(String auditType) {
        registry.counter("pdp.audit.failure", "type", auditType).increment();
    }
}
