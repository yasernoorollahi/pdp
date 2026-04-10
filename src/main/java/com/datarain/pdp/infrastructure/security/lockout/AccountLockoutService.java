package com.datarain.pdp.infrastructure.security.lockout;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * اضافه شد: سرویس Account Lockout Policy
 * بعد از MAX_FAILED_ATTEMPTS بار تلاش ناموفق، اکانت به مدت LOCK_DURATION_MINUTES قفل میشه
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    private final UserRepository userRepository;
    private final SecurityAuditService securityAuditService;
    private final LockoutProperties lockoutProperties;

    @Transactional
    public void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= lockoutProperties.getMaxFailedAttempts() && user.getLockedUntil() == null) {
            user.setLockedUntil(Instant.now().plus(lockoutProperties.getDuration()));
            log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
            securityAuditService.log(
                    SecurityEventType.ACCOUNT_LOCKED,
                    user.getEmail(),
                    user.getId(),
                    null,
                    null,
                    "Account locked after " + attempts + " failed attempts",
                    true
            );
        }

        userRepository.save(user);
    }

    @Transactional
    public void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    public void checkAccountLocked(User user, String ipAddress, String userAgent) {
        if (user.isLocked()) {
            log.warn("Login attempt on locked account: {}", user.getEmail());
            securityAuditService.log(
                    SecurityEventType.LOGIN_BLOCKED,
                    user.getEmail(),
                    user.getId(),
                    ipAddress,
                    userAgent,
                    "Blocked login attempt on locked account",
                    false
            );
            throw new BaseBusinessException(
                    ErrorCode.FORBIDDEN,
                    HttpStatus.FORBIDDEN,
                    "Account is temporarily locked. Please try again later.") {};
        }
    }
}
