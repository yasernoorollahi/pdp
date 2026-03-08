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
    @Getter private final Counter itemCreatedCounter;
    @Getter private final Counter itemArchivedCounter;
    @Getter private final Counter rateLimitHitCounter;
    @Getter private final Counter tokenRefreshCounter;
    @Getter private final Counter moderationCaseCreatedCounter;
    @Getter private final Counter moderationCaseStateTransitionCounter;
    @Getter private final Counter moderationCaseAutoBlockedCounter;
    @Getter private final Counter extractionRequestedCounter;
    @Getter private final Counter extractionFailedCounter;
    @Getter private final Counter userMessageCreatedCounter;
    @Getter private final Counter userMessageUpdatedCounter;
    @Getter private final Counter userMessageDeletedCounter;
    @Getter private final Counter userMessageProcessedCounter;
    @Getter private final Counter signalEngineSuccessCounter;
    @Getter private final Counter signalEngineFailureCounter;
    @Getter private final Counter signalEngineSignalsStoredCounter;

    // timers - برای اندازه‌گیری زمان عملیات‌های مهم
    @Getter private final Timer itemCreateTimer;
    @Getter private final Timer authLoginTimer;

    public PdpMetrics(MeterRegistry registry) {
        this.loginSuccessCounter = Counter.builder("pdp.auth.login.success")
                .description("تعداد ورود موفق")
                .register(registry);

        this.loginFailedCounter = Counter.builder("pdp.auth.login.failed")
                .description("تعداد ورود ناموفق")
                .register(registry);

        this.itemCreatedCounter = Counter.builder("pdp.item.created")
                .description("تعداد item های ساخته شده")
                .register(registry);

        this.itemArchivedCounter = Counter.builder("pdp.item.archived")
                .description("تعداد item های آرشیو شده")
                .register(registry);

        this.rateLimitHitCounter = Counter.builder("pdp.rate_limit.hit")
                .description("تعداد دفعاتی که rate limit رد شد")
                .register(registry);

        this.tokenRefreshCounter = Counter.builder("pdp.auth.token.refresh")
                .description("تعداد دفعات refresh token")
                .register(registry);

        this.moderationCaseCreatedCounter = Counter.builder("pdp.moderation.case.created")
                .description("تعداد پرونده‌های moderation ایجاد شده")
                .register(registry);

        this.moderationCaseStateTransitionCounter = Counter.builder("pdp.moderation.case.state.transition")
                .description("تعداد تغییر وضعیت پرونده‌های moderation")
                .register(registry);

        this.moderationCaseAutoBlockedCounter = Counter.builder("pdp.moderation.case.auto_blocked")
                .description("تعداد پرونده‌های auto-block شده")
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

        this.itemCreateTimer = Timer.builder("pdp.item.create.duration")
                .description("زمان ساخت item")
                .register(registry);

        this.authLoginTimer = Timer.builder("pdp.auth.login.duration")
                .description("زمان پردازش login")
                .register(registry);
    }
}
