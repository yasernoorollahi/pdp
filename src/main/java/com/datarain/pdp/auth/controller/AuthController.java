package com.datarain.pdp.auth.controller;

import com.datarain.pdp.auth.dto.AuthResponse;
import com.datarain.pdp.auth.dto.LoginRequest;
import com.datarain.pdp.auth.dto.RefreshRequest;
import com.datarain.pdp.auth.dto.RegisterRequest;
import com.datarain.pdp.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request,
                                 HttpServletRequest httpRequest) {
        return authService.register(request, httpRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request,
                              HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody @Valid RefreshRequest request, HttpServletRequest httpRequest) {
        return authService.refresh(request, httpRequest);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public void logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
    }

    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public void logoutAll(HttpServletRequest httpRequest) {
        authService.logoutAll(httpRequest);
    }
}
