package com.datarain.pdp.infrastructure.security;

import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // اصلاح شد: اضافه کردن enabled و accountNonLocked به CustomUserDetails
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .toList(),
                user.isEnabled(),
                !user.isLocked()   // اگه قفله، accountNonLocked = false
        );
    }
}
