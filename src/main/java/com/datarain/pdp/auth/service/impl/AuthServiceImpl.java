package com.datarain.pdp.auth.service.impl;

import com.datarain.pdp.auth.dto.AuthResponse;
import com.datarain.pdp.auth.dto.LoginRequest;
import com.datarain.pdp.auth.dto.RefreshRequest;
import com.datarain.pdp.auth.dto.RegisterRequest;
import com.datarain.pdp.auth.entity.RefreshToken;
import com.datarain.pdp.auth.service.AuthService;
import com.datarain.pdp.auth.service.RefreshTokenService;
import com.datarain.pdp.exception.business.AccountDisabledException;
import com.datarain.pdp.exception.business.DuplicateUserException;
import com.datarain.pdp.exception.business.InvalidCredentialsException;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.Role;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.infrastructure.security.jwt.JwtService;
import com.datarain.pdp.infrastructure.security.lockout.AccountLockoutService;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    // اضافه شد: سرویس‌های جدید
    private final AccountLockoutService lockoutService;
    private final SecurityAuditService auditService;
    private final PdpMetrics metrics;

    @Override
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.ROLE_USER));
        userRepository.save(user);

        String device = httpRequest.getHeader("User-Agent");
        String ip = httpRequest.getRemoteAddr();

        // اضافه شد: ثبت رویداد register در audit log
        auditService.log(SecurityEventType.REGISTER, user.getEmail(), user.getId(), ip, device, null, true);

        RefreshToken rt = refreshTokenService.create(user, device, ip);
        String accessToken = jwtService.generateAccessToken(user);

        log.info("New user registered: {}", request.email());
        return new AuthResponse(accessToken, rt.getToken());
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Instant startedAt = Instant.now();
        try {
            String ip = httpRequest.getRemoteAddr();
            String device = httpRequest.getHeader("User-Agent");

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> {
                        auditService.log(SecurityEventType.LOGIN_FAILED, request.email(), null, ip, device,
                                "User not found", false);
                        metrics.getLoginFailedCounter().increment();
                        return new InvalidCredentialsException();
                    });

            lockoutService.checkAccountLocked(user);

            if (!user.isEnabled()) {
                throw new AccountDisabledException();
            }

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                lockoutService.recordFailedAttempt(user);
                auditService.log(SecurityEventType.LOGIN_FAILED, user.getEmail(), user.getId(),
                        ip, device, "Wrong password", false);
                metrics.getLoginFailedCounter().increment();
                log.warn("Failed login for: {}", request.email());
                throw new InvalidCredentialsException();
            }

            lockoutService.resetFailedAttempts(user);
            auditService.log(SecurityEventType.LOGIN_SUCCESS, user.getEmail(), user.getId(), ip, device, null, true);
            metrics.getLoginSuccessCounter().increment();

            RefreshToken rt = refreshTokenService.create(user, device, ip);
            String accessToken = jwtService.generateAccessToken(user);

            log.info("User logged in: {}", request.email());
            return new AuthResponse(accessToken, rt.getToken());
        } finally {
            metrics.getAuthLoginTimer().record(Duration.between(startedAt, Instant.now()));
        }
    }

    @Override
    public AuthResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {
        RefreshToken old = refreshTokenService.verify(request.refreshToken());
        refreshTokenService.revoke(old);
        RefreshToken fresh = refreshTokenService.rotate(old);
        String newAccess = jwtService.generateAccessToken(old.getUser());

        auditService.log(SecurityEventType.TOKEN_REFRESH,
                old.getUser().getEmail(), old.getUser().getId(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"),
                null, true);
        metrics.getTokenRefreshCounter().increment();

        log.info("Refresh token rotated for userId={}", old.getUser().getId());
        return new AuthResponse(newAccess, fresh.getToken());
    }

    @Override
    public void logout(HttpServletRequest httpRequest) {
        UUID userId = SecurityUtils.currentUserId();
        String email = SecurityUtils.currentUsername();
        refreshTokenService.revokeAllForUser(userId);
        auditService.log(SecurityEventType.LOGOUT, email, userId,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"),
                null, true);
        log.info("User logout processed for userId={}", userId);
    }

    @Override
    public void logoutAll(HttpServletRequest httpRequest) {
        UUID userId = SecurityUtils.currentUserId();
        String email = SecurityUtils.currentUsername();
        refreshTokenService.revokeAllForUser(userId);
        auditService.log(SecurityEventType.LOGOUT_ALL_DEVICES, email, userId,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"),
                null, true);
        log.info("Logout-all processed for userId={}", userId);
    }
}
