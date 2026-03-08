package com.datarain.pdp.user.service;

import com.datarain.pdp.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserResponse> findAll(Pageable pageable);
    UserResponse me();
    UserResponse getById(UUID id);
    void setEnabled(UUID id, boolean enabled);
    void unlock(UUID id);
}
