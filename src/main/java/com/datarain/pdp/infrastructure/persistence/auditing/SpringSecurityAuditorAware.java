package com.datarain.pdp.infrastructure.persistence.auditing;

import com.datarain.pdp.infrastructure.security.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<UUID> {



    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails user) {
            return Optional.of(user.getUserId());
        }

        return Optional.empty();
    }
}

