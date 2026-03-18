# راهنمای پروژه PDP (Personal Data Platform)

این فایل بر اساس سورس فعلی پروژه بازنویسی شده و تمام فیچرها و رفتارهای موجود را پوشش می‌دهد.

## تصویر کلی
این پروژه یک REST API مبتنی بر Spring Boot است که این بخش‌ها را دارد:
- احراز هویت با JWT و Refresh Token
- مدیریت آیتم‌ها (Items) با آرشیو نرم و بازیابی
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
- بعد از ۵ تلاش ناموفق (`MAX_FAILED_ATTEMPTS = 5`) اکانت برای ۱۵ دقیقه قفل می‌شود.
- قفل شدن با مقداردهی `lockedUntil = now + 15min` انجام می‌شود.
- در هر لاگین موفق، شمارنده تلاش‌های ناموفق ریست می‌شود.
- اگر اکانت قفل باشد، لاگین با خطای `FORBIDDEN` رد می‌شود.
- ادمین می‌تواند اکانت را دستی باز کند:
  - `POST /api/users/{id}/unlock`

### فعال/غیرفعال کردن اکانت
- ادمین می‌تواند اکانت را disable/enable کند:
  - `PATCH /api/users/{id}/enabled?enabled=true|false`

### نکته مهم درباره Retry
واژه `retry` در پروژه مربوط به **Resilience4j** است.
هیچ Retry خودکار برای ورود کاربران وجود ندارد.

## Rate Limit
- پیاده‌سازی فعلی In‑Memory است (`InMemoryRateLimitService`).
- کلید محدودسازی:
  - اگر کاربر لاگین باشد: `auth.getName()`
  - در غیر این صورت: `IP`
- سیاست‌ها (`RateLimitPolicyProvider`):
  - `/api/auth/login` → 50 درخواست در ۱ دقیقه
  - `/api/auth/**` → 20 درخواست در ۱ دقیقه
  - `/api/items/**` → 100 درخواست در ۱ دقیقه
  - سایر مسیرها → 50 درخواست در ۱ دقیقه
- در صورت عبور از حد، پاسخ 429 با JSON تمیز برمی‌گردد.

## Items (آیتم‌ها)
### ایجاد و مدیریت
- `POST /api/items` (ROLE_USER یا ROLE_ADMIN)
- `GET /api/items/{id}`
- `DELETE /api/items/{id}` (فقط ADMIN) → آرشیو نرم
- `PUT /api/items/{id}/restore` (فقط ADMIN)

### لیست و جستجو
- `GET /api/items` با پشتیبانی از:
  - pagination (`page`, `size`, `sort`)
  - فیلتر نوع (`type`)
  - جستجو در عنوان (`search`)
- از Specification Pattern استفاده شده تا ترکیب فیلترها منعطف باشد.

### وضعیت آیتم‌ها
- `ACTIVE`
- `ARCHIVED`

### Domain Events
- هنگام ساخت آیتم: `ItemCreatedEvent`
- هنگام آرشیو: `ItemArchivedEvent`

## کاربران
- `GET /api/users` (ادمین) → لیست کاربران با DTO امن
- `GET /api/users/me` → اطلاعات کاربر جاری
- `GET /api/users/{id}` (ادمین)
- `PATCH /api/users/{id}/enabled` (ادمین)
- `POST /api/users/{id}/unlock` (ادمین)

## Admin
- `GET /api/admin/stats` (ROLE_ADMIN)
  - آمار کاربران (کل، فعال، قفل‌شده)
  - آمار آیتم‌ها (کل، فعال، آرشیو)
  - آمار توکن‌ها (کل، فعال)
  - وضعیت حافظه JVM

## لاگ امنیتی (Security Audit Log)
- همه رویدادهای امنیتی به صورت Async ذخیره می‌شوند.
- جدول جداگانه `security_audit_logs` دارد.
- رویدادهای مهم: `LOGIN_SUCCESS`, `LOGIN_FAILED`, `TOKEN_REFRESH`, ...

## Metrics و مانیتورینگ
### Metrics سفارشی (Micrometer)
- `pdp.auth.login.success`
- `pdp.auth.login.failed`
- `pdp.item.created`
- `pdp.item.archived`
- `pdp.rate_limit.hit`
- `pdp.auth.token.refresh`
- تایمرها:
  - `pdp.item.create.duration`
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

- **ExpireArchivedItemsJob**
  - هر روز ساعت ۳ بامداد اجرا می‌شود.
  - آیتم‌های آرشیو شده قدیمی‌تر از ۳۰ روز حذف می‌شوند.

- **TestDataSeedingJob**
  - زمان‌بندی‌شده نیست و فقط در startup اجرا می‌شود.
  - به صورت پیش‌فرض خاموش است (`jobs.test-data.enabled: false`).
  - وقتی روشن شود، داده‌ی متنوع برای جدول‌های `users`, `items`, `refresh_tokens`, `notifications`, `security_audit_logs`, `job_execution_log` می‌سازد.
  - برای جلوگیری از تکرار، اگر `seed.user.00@pdp.local` وجود داشته باشد اجرا را Skip می‌کند.
  - حجم دیتا با این تنظیمات کنترل می‌شود:
    - `jobs.test-data.users`
    - `jobs.test-data.items-per-user`
    - `jobs.test-data.refresh-tokens-per-user`

## Resilience4j (نمونه خارجی)
  - دارای Circuit Breaker، Retry و TimeLimiter
  - Retry فقط برای این مسیر است (نه login)

## خطاها و Exception Handling
- Error Response استاندارد با این ساختار:
  - `timestamp`, `status`, `error`, `code`, `message`, `path`, `traceId`
- خطاهای اعتبارسنجی به صورت لیست فیلدها برمی‌گردند.
- `traceId` از `TraceIdFilter` در Header و MDC اضافه می‌شود.

## پایگاه داده
- Flyway فعال است (`db/migration/V1__baseline.sql`).
- جداول اصلی:
  - `users`, `user_roles`
  - `items`
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
- Redis (فعلاً برای Rate Limit استفاده نشده)
- Prometheus
- Grafana
- App

نکته: در `application.yml` دیتابیس پیش‌فرض `pdp_upgrade` است ولی Docker روی `pdp` بالا می‌آید.
اگر با Docker اجرا می‌کنید، مقدار `SPRING_DATASOURCE_URL` در compose ست شده است.

---

اگر بخواهی همین محتوا را به اسم `HELM.md` بسازم یا نام فایل را تغییر دهم، بگو تا انجام بدهم.
