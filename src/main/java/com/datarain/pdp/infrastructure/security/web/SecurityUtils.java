package com.datarain.pdp.infrastructure.security.web;

import com.datarain.pdp.infrastructure.security.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;


public final class SecurityUtils {

    private SecurityUtils()
    {
    }


    public static CustomUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            throw new IllegalStateException("No authenticated user");
        }

        return user;
    }


    public static String currentUsername() {
        return currentUser().getUsername();
    }

    public static Collection<? extends GrantedAuthority> currentAuthorities() {
        return currentUser().getAuthorities();
    }


    public static boolean hasAnyRole(String... roles) {
        Set<String> roleSet = Set.of(roles);

        return currentAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleSet::contains);
    }



    public static UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("No authenticated user");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    }



    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }






}

