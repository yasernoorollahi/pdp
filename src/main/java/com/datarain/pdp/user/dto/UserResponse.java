package com.datarain.pdp.user.dto;

import com.datarain.pdp.infrastructure.security.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * اضافه شد: DTO برای User به جای اینکه entity مستقیم برگردونیم
 * این مهمه چون passwordHash نباید هیچوقت توی response باشه
 */
public record UserResponse(
        UUID id,
        String email,
        Set<Role> roles,
        boolean enabled,
        boolean emailVerified,
        int failedLoginAttempts,
        boolean locked,
        Instant createdAt
) {}
