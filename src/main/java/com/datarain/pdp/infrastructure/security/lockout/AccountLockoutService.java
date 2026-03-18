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

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final SecurityAuditService securityAuditService;

    @Transactional
    public void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS && user.getLockedUntil() == null) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES));
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

    public void checkAccountLocked(User user) {
        if (user.isLocked()) {
            log.warn("Login attempt on locked account: {}", user.getEmail());
            throw new BaseBusinessException(
                    ErrorCode.FORBIDDEN,
                    HttpStatus.FORBIDDEN,
                    "Account is temporarily locked. Please try again after " + LOCK_DURATION_MINUTES + " minutes.") {};
        }
    }
}
