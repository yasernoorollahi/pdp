package com.datarain.pdp.user.service.impl;

import com.datarain.pdp.exception.business.UserNotFoundException;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditService;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import com.datarain.pdp.user.dto.UserResponse;
import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.mapper.UserMapper;
import com.datarain.pdp.user.repository.UserRepository;
import com.datarain.pdp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityAuditService securityAuditService;

    @Override
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    public UserResponse me() {
        UUID userId = SecurityUtils.currentUserId();
        return userMapper.toResponse(findUserById(userId));
    }

    @Override
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Override
    @Transactional
    public void setEnabled(UUID id, boolean enabled) {
        User user = findUserById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
        securityAuditService.log(
                enabled ? SecurityEventType.ACCOUNT_ENABLED : SecurityEventType.ACCOUNT_DISABLED,
                user.getEmail(),
                user.getId(),
                null,
                null,
                "Account status changed by admin",
                true
        );
    }

    @Override
    @Transactional
    public void unlock(UUID id) {
        User user = findUserById(id);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        securityAuditService.log(
                SecurityEventType.ACCOUNT_UNLOCKED,
                user.getEmail(),
                user.getId(),
                null,
                null,
                "Account unlocked by admin",
                true
        );
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
