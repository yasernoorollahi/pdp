# راهنمای پروژه PDP (Personal Data Platform)

این فایل بر اساس سورس فعلی پروژه بازنویسی شده و تمام فیچرها و رفتارهای موجود را پوشش می‌دهد.

## تصویر کلی
این پروژه یک REST API مبتنی بر Spring Boot است که این بخش‌ها را دارد:
- احراز هویت با JWT و Refresh Token
- مدیریت کاربران و نقش‌ها (ROLE_USER/ROLE_ADMIN)
- لاگ و لاگ امنیتی (Audit)
- Rate Limit روی APIها
- Metrics و Health Check برای مانیتورینگ
- Jobهای زمان‌بندی‌شده برای پاکسازی
- یک کلاینت خارجی نمونه با Resilience4j

## احراز هویت و امنیت
### ثبت‌نام و ورود
- `POST /api/auth/register`
  - ثبت کاربر جدید با ایمیل و پسورد.
  - ثبت رویداد امنیتی `REGISTER`.
  - ساخت Access Token و Refresh Token.

- `POST /api/auth/login`
  - اعتبارسنجی پسورد.
  - ثبت رویداد امنیتی موفق/ناموفق.
  - ساخت Access Token و Refresh Token.

### JWT
- Access Token شامل `email`, `userId`, `roles` است.
- `pdp.jwt.secret` اجباری است و باید حداقل ۳۲ کاراکتر باشد.
- زمان انقضای Access Token از کانفیگ خوانده می‌شود:
  - `pdp.jwt.access-token-expiration-ms` پیش‌فرض `900000` میلی‌ثانیه (۱۵ دقیقه).

### Refresh Token
- در ثبت‌نام و ورود: Refresh Token با عمر ۷ روز ساخته می‌شود.
- در `POST /api/auth/refresh`:
  - Refresh Token قبلی Verify می‌شود.
  - قبلی Revoke می‌شود.
  - Refresh Token جدید ساخته می‌شود (عمر ۳۰ روز).
  - رویداد `TOKEN_REFRESH` ثبت می‌شود.

### Logout
- `POST /api/auth/logout`
  - همه توکن‌های فعال کاربر جاری را Revoke می‌کند.
  - رویداد `LOGOUT` ثبت می‌شود.

- `POST /api/auth/logout-all`
  - مشابه بالا، رویداد `LOGOUT_ALL_DEVICES` ثبت می‌شود.

### قفل شدن اکانت (Account Lockout)
منطق اصلی در `AccountLockoutService` پیاده شده است:
- به صورت پیش‌فرض بعد از ۵ تلاش ناموفق اکانت برای ۱۵ دقیقه قفل می‌شود.
- این مقادیر از کانفیگ می‌آیند:
  - `pdp.security.lockout.max-failed-attempts`
  - `pdp.security.lockout.duration`
- قفل شدن با مقداردهی `lockedUntil = now + duration` انجام می‌شود.
- در هر لاگین موفق، شمارنده تلاش‌های ناموفق ریست می‌شود.
- اگر اکانت قفل باشد، لاگین با خطای `FORBIDDEN` رد می‌شود.
- تلاش ورود روی اکانت قفل‌شده با رویداد `LOGIN_BLOCKED` هم audit می‌شود.
- ادمین می‌تواند اکانت را دستی باز کند:
  - `POST /api/users/{id}/unlock`

### فعال/غیرفعال کردن اکانت
- ادمین می‌تواند اکانت را disable/enable کند:
  - `PATCH /api/users/{id}/enabled?enabled=true|false`

### نکته مهم درباره Retry
واژه `retry` در پروژه مربوط به **Resilience4j** است.
هیچ Retry خودکار برای ورود کاربران وجود ندارد.

## Rate Limit
- پیاده‌سازی اصلی Redis-based است (`RedisRateLimitService`) و اگر Redis در دسترس نباشد به `InMemoryRateLimitService` fallback می‌کند.
- کلید محدودسازی:
  - بسته به policy می‌تواند `USER`, `IP`, یا `USER_OR_IP` باشد.
  - برای localhost، loopback normalize می‌شود و key تمیزتری مثل `127.0.0.1` می‌سازد.
- سیاست‌ها (`RateLimitPolicyProvider`) از `pdp.rate-limit.*` می‌آیند و پیش‌فرض‌های مهمشان این‌هاست:
  - `/api/auth/login` → 10 درخواست در ۱ دقیقه بر اساس IP
  - `/api/auth/refresh` → 30 درخواست در ۱ دقیقه
  - `/api/user-messages` → 120 درخواست در ۱ دقیقه
  - `/api/extraction` → 30 درخواست در ۱ دقیقه
- در صورت عبور از حد، پاسخ 429 با JSON برمی‌گردد و headerهای `Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset` هم ست می‌شوند.

## کاربران
- `GET /api/users` (ادمین) → لیست کاربران با DTO امن
- `GET /api/users/me` → اطلاعات کاربر جاری
- `GET /api/users/{id}` (ادمین)
- `PATCH /api/users/{id}/enabled` (ادمین)
- `POST /api/users/{id}/unlock` (ادمین)

## Admin
- `GET /api/admin/stats` (ROLE_ADMIN)
  - آمار کاربران (کل، فعال، قفل‌شده)
  - آمار توکن‌ها (کل، فعال)
  - وضعیت حافظه JVM

## لاگ امنیتی (Security Audit Log)
- همه رویدادهای امنیتی به صورت Async ذخیره می‌شوند.
- Async execution از executor اختصاصی با MDC propagation استفاده می‌کند تا `traceId` در branchهای async هم حفظ شود.
- جدول جداگانه `security_audit_logs` دارد.
- رویدادهای مهم: `LOGIN_SUCCESS`, `LOGIN_FAILED`, `LOGIN_BLOCKED`, `TOKEN_REFRESH`, ...

## Metrics و مانیتورینگ
### Metrics سفارشی (Micrometer)
- `pdp.auth.login.success`
- `pdp.auth.login.failed`
- `pdp.rate_limit.hit`
- `pdp.rate_limit.rejected`
- `pdp.audit.failure`
- `pdp.auth.token.refresh`
- تایمرها:
  - `pdp.auth.login.duration`

### Actuator
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

### Health Indicators سفارشی
- Application Info (زمان شروع و uptime)
- Database health (اعتبار کانکشن)

## Jobهای زمان‌بندی‌شده
- **PurgeExpiredRefreshTokensJob**
  - هر ساعت اجرا می‌شود.
  - توکن‌های منقضی را حذف می‌کند.
  - با `jobs.refresh-token.enabled` قابل کنترل است.

- **TestDataSeedingJob**
  - زمان‌بندی‌شده نیست و فقط در startup اجرا می‌شود.
  - به صورت پیش‌فرض خاموش است (`jobs.test-data.enabled: false`).
  - وقتی روشن شود، داده‌ی متنوع برای جدول‌های `users`, `refresh_tokens`, `notifications`, `security_audit_logs`, `job_execution_log` می‌سازد.
  - برای جلوگیری از تکرار، اگر `seed.user.00@pdp.local` وجود داشته باشد اجرا را Skip می‌کند.
  - حجم دیتا با این تنظیمات کنترل می‌شود:
    - `jobs.test-data.users`
    - `jobs.test-data.refresh-tokens-per-user`

## Resilience4j (نمونه خارجی)
  - دارای Circuit Breaker، Retry و TimeLimiter
  - Retry فقط برای این مسیر است (نه login)

## خطاها و Exception Handling
- Error Response استاندارد با این ساختار:
  - `timestamp`, `status`, `error`, `code`, `message`, `path`, `traceId`
- خطاهای اعتبارسنجی به صورت لیست فیلدها برمی‌گردند.
- `traceId` از `TraceIdFilter` در Header و MDC اضافه می‌شود.
- اگر کلاینت `X-Trace-Id` بفرستد، مقدارش normalize می‌شود و دوباره در response برگردانده می‌شود.

## پایگاه داده
- Flyway فعال است (`db/migration/V1__baseline.sql`).
- جداول اصلی:
  - `users`, `user_roles`
  - `refresh_tokens`
  - `security_audit_logs`
- یک کاربر ادمین اولیه با ایمیل `admin@pdp.local` ایجاد می‌شود.
  - رمز خام در سورس مشخص نشده و از روی hash قابل استخراج نیست.

## Swagger
- UI در مسیر `/swagger` فعال است.
- JWT با Bearer تعریف شده است.

## راه‌اندازی محلی (Docker Compose)
فایل `docker-compose.yml` شامل این سرویس‌ها است:
- PostgreSQL
- Redis (برای Rate Limit توزیع‌شده)
- Prometheus
- Grafana
- App

نکته:
- در config پایه، `spring.flyway.clean-disabled=true` نگه داشته شده تا پاک‌کردن migration state به‌صورت ناخواسته در محیط‌های واقعی باز نباشد.
- اگر با Docker اجرا می‌کنید، مقدار `SPRING_DATASOURCE_URL` در compose ست شده است.

---

اگر بخواهی همین محتوا را به اسم `HELM.md` بسازم یا نام فایل را تغییر دهم، بگو تا انجام بدهم.
