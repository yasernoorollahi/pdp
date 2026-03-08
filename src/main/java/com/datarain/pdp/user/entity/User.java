package com.datarain.pdp.user.entity;

import com.datarain.pdp.common.AuditableEntity;
import com.datarain.pdp.infrastructure.security.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    private Set<Role> roles;

    // اضافه شد: برای Account Lockout Policy - تعداد تلاش‌های ناموفق ورود
    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    // اضافه شد: زمانی که قفل اکانت برداشته میشه - null یعنی قفل نیست
    @Column
    private Instant lockedUntil;

    // اضافه شد: ایمیل تأیید شده است؟
    @Column(nullable = false)
    private boolean emailVerified = false;

    // اضافه شد: توکن تأیید ایمیل
    @Column
    private String emailVerificationToken;

    // اضافه شد: توکن ریست پسورد
    @Column
    private String passwordResetToken;

    // اضافه شد: زمان انقضای توکن ریست پسورد
    @Column
    private Instant passwordResetTokenExpiry;

    // اضافه شد: آیا اکانت قفل شده؟
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }
}
