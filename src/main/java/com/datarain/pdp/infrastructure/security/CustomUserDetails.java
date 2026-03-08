package com.datarain.pdp.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    // اضافه شد: وضعیت قفل بودن اکانت
    private final boolean accountNonLocked;

    public CustomUserDetails(UUID id, String email, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean enabled, boolean accountNonLocked) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
    }

    public UUID getUserId() {
        return id;
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    // اصلاح شد: این قبلاً همیشه true بود، حالا از user.isEnabled() میخونه
    @Override public boolean isEnabled() { return enabled; }
    // اضافه شد: اکانت قفل نباشه
    @Override public boolean isAccountNonLocked() { return accountNonLocked; }
}
