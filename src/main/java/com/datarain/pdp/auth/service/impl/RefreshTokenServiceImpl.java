package com.datarain.pdp.auth.service.impl;

import com.datarain.pdp.auth.entity.RefreshToken;
import com.datarain.pdp.auth.repository.RefreshTokenRepository;
import com.datarain.pdp.auth.service.RefreshTokenService;
import com.datarain.pdp.exception.business.InvalidTokenException;
import com.datarain.pdp.exception.business.TokenExpiredException;
import com.datarain.pdp.exception.business.TokenRevokedException;
import com.datarain.pdp.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public RefreshToken create(User user, String device, String ipAddress) {

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        rt.setRevoked(false);
        rt.setDevice(device);
        rt.setIpAddress(ipAddress);

        return refreshTokenRepository.save(rt);
    }

    @Override
    public RefreshToken verify(String token) {

        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(InvalidTokenException::new);

        if (rt.isRevoked()) {
            throw new TokenRevokedException();
        }

        if (rt.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException();
        }

        return rt;
    }

    @Override
    @Transactional
    public RefreshToken rotate(RefreshToken old) {

        RefreshToken fresh = new RefreshToken();
        fresh.setUser(old.getUser());
        fresh.setToken(UUID.randomUUID().toString());
        fresh.setDevice(old.getDevice());
        fresh.setIpAddress(old.getIpAddress());
        fresh.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));
        fresh.setRevoked(false);

        return refreshTokenRepository.save(fresh);
    }

    @Override
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
        log.info("Refresh token revoked for userId={}", token.getUser().getId());
    }


    @Override
    public void revokeByToken(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(InvalidTokenException::new);

        rt.setRevoked(true);
        rt.setRevokedAt(Instant.now());
        refreshTokenRepository.save(rt);
        log.info("Refresh token revoked by token for userId={}", rt.getUser().getId());
    }

    @Override
    public void revokeAllForUser(User user) {
        List<RefreshToken> tokens =
                refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        for (RefreshToken t : tokens) {
            t.setRevoked(true);
            t.setRevokedAt(Instant.now());
        }

        refreshTokenRepository.saveAll(tokens);
        log.info("Revoked {} refresh tokens for userId={}", tokens.size(), user.getId());
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        List<RefreshToken> tokens =
                refreshTokenRepository.findAllByUser_IdAndRevokedFalse(userId);

        for (RefreshToken t : tokens) {
            t.setRevoked(true);
            t.setRevokedAt(Instant.now());
        }

        refreshTokenRepository.saveAll(tokens);
        log.info("Revoked {} refresh tokens for userId={}", tokens.size(), userId);
    }

    @Override
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }


}
