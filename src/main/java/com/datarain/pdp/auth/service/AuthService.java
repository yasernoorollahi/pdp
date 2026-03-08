package com.datarain.pdp.auth.service;


import com.datarain.pdp.auth.dto.LoginRequest;
import com.datarain.pdp.auth.dto.RefreshRequest;
import com.datarain.pdp.auth.dto.RegisterRequest;
import com.datarain.pdp.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refresh(RefreshRequest request, HttpServletRequest httpRequest);

    void logout(HttpServletRequest httpRequest);

    void logoutAll(HttpServletRequest httpRequest);

}
