package com.datarain.pdp.user.repository;

import com.datarain.pdp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// اضافه شد: متدهای count برای admin stats و lockout
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByEmailStartingWithOrderByEmailAsc(String prefix);

    long countByEnabled(boolean enabled);

    // اضافه شد: شمارش user هایی که lockedUntil آنها در آینده است (یعنی الان قفل هستند)
    long countByLockedUntilAfter(Instant now);
}
