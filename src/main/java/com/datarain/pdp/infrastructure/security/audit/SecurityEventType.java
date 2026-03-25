package com.datarain.pdp.infrastructure.security.audit;

/**
 * اضافه شد: انواع رویدادهای امنیتی که لاگ میشن
 */
public enum SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    LOGOUT_ALL_DEVICES,
    TOKEN_REFRESH,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ACCOUNT_ENABLED,
    ACCOUNT_DISABLED,
    REGISTER,
    ADMIN_JOB_CONTROL_UPDATED
}
