package com.datarain.pdp.auth.service;

import com.datarain.pdp.auth.entity.RefreshToken;
import com.datarain.pdp.user.entity.User;

import java.util.UUID;

public interface RefreshTokenService {

//    RefreshToken create(User user);
    RefreshToken create(User user, String device, String ipAddress);

    RefreshToken verify(String token);

    void revoke(RefreshToken token);

    RefreshToken rotate(RefreshToken old);
    void deleteByUser(User user);

    void revokeByToken(String token);

    void revokeAllForUser(User user);

    void revokeAllForUser(UUID userId);

}