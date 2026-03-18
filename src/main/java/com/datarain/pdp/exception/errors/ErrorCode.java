package com.datarain.pdp.exception.errors;


public enum ErrorCode {

    /* =========================
       Validation / Request Errors (400)
       ========================= */
    // وقتی مقدار فیلدها با @Valid یا قوانین ولیدیشن نمی‌خونه
    VALIDATION_ERROR,

    // وقتی ساختار JSON یا فرمت ورودی کلاً غلطه (enum اشتباه، JSON خراب، تاریخ اشتباه)
    INVALID_REQUEST,

    // وقتی متد HTTP اشتباه زده شده (مثلاً POST به جای GET)
    METHOD_NOT_ALLOWED,

    // وقتی مسیر یا resource اصلاً وجود نداره
    NOT_FOUND,


    /* =========================
       Business Errors (Domain level)
       ========================= */

    DUPLICATE_USER,
    // قانون بیزینسی نقض شده (مثلاً وضعیت آیتم اجازه حذف نمی‌دهد)
    BUSINESS_RULE_VIOLATION,

    // عملیات روی این منبع مجاز نیست
    OPERATION_NOT_ALLOWED,
    //این برای محدودیت فراخوانی هر api است
    RATE_LIMIT_EXCEEDED,

    /* =========================
       Security / Authorization Errors
       ========================= */

    // کاربر لاگین نکرده یا توکن نامعتبر است
    UNAUTHORIZED,

    // کاربر لاگین هست ولی دسترسی به این منبع ندارد
    FORBIDDEN,

    // توکن منقضی شده
    TOKEN_EXPIRED,

    // توکن معتبر نیست
    INVALID_TOKEN,


    /* =========================
       Persistence / Database Errors
       ========================= */

    // خطای کلی دیتابیس
    DATABASE_ERROR,

    // نقض constraint (foreign key, unique, not null در دیتابیس)
    DATA_INTEGRITY_VIOLATION,

    // اتصال به دیتابیس قطع شده یا تایم‌اوت
    DATABASE_CONNECTION_FAILED,


    /* =========================
       External / Integration Errors
       ========================= */

    // سرویس خارجی در دسترس نیست
    DOWNSTREAM_SERVICE_UNAVAILABLE,

    // سرویس خارجی خطای 4xx یا 5xx داده
    DOWNSTREAM_SERVICE_ERROR,

    // تایم‌اوت در ارتباط با سرویس دیگر
    DOWNSTREAM_TIMEOUT,


    /* =========================
       System / Internal Errors (500)
       ========================= */

    // خطای پیش‌بینی‌نشده داخلی
    INTERNAL_ERROR,

    // خطای ناشناخته که دسته‌بندی نشده
    UNKNOWN_ERROR
}
