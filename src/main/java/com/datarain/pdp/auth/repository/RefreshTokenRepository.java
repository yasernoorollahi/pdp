package com.datarain.pdp.auth.repository;

import com.datarain.pdp.auth.entity.RefreshToken;
import com.datarain.pdp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser_Id(UUID userId);

    void deleteByUser(User user);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    List<RefreshToken> findAllByUser_IdAndRevokedFalse(UUID userId);

    long deleteByExpiryDateBefore(Instant now);

    // اضافه شد: برای admin stats - توکن‌های فعال (revoke نشده و منقضی نشده)
    long countByRevokedFalseAndExpiryDateAfter(Instant now);
}
