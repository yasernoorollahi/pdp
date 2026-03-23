package com.datarain.pdp.auth.controller;

import com.datarain.pdp.auth.dto.AuthResponse;
import com.datarain.pdp.auth.dto.LoginRequest;
import com.datarain.pdp.auth.dto.RefreshRequest;
import com.datarain.pdp.auth.dto.RegisterRequest;
import com.datarain.pdp.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication, token lifecycle, and logout operations.")
public class AuthController {

    private final AuthService authService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    @Operation(summary = "Register a new user and issue access and refresh tokens.")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request,
                                 HttpServletRequest httpRequest) {
        return authService.register(request, httpRequest);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and issue access and refresh tokens.")
    public AuthResponse login(@RequestBody @Valid LoginRequest request,
                              HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and return a new access token.")
    public AuthResponse refresh(@RequestBody @Valid RefreshRequest request, HttpServletRequest httpRequest) {
        return authService.refresh(request, httpRequest);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @Operation(summary = "Revoke active refresh tokens for the current user.")
    public void logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
    }

    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @Operation(summary = "Revoke all refresh tokens across all user devices.")
    public void logoutAll(HttpServletRequest httpRequest) {
        authService.logoutAll(httpRequest);
    }
}
