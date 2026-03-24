package com.datarain.pdp.web.auth;

import com.datarain.pdp.auth.controller.AuthController;
import com.datarain.pdp.auth.dto.AuthResponse;
import com.datarain.pdp.auth.dto.LoginRequest;
import com.datarain.pdp.auth.dto.RefreshRequest;
import com.datarain.pdp.auth.dto.RegisterRequest;
import com.datarain.pdp.auth.service.AuthService;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import com.datarain.pdp.support.TestExpectations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = AuthController.class)
class AuthControllerTest extends AbstractWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_register_user() throws Exception {
        RegisterRequest request = new RegisterRequest("user@pdp.local", "password123");
        when(authService.register(any(RegisterRequest.class), any()))
                .thenReturn(new AuthResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(201))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void should_login_user() throws Exception {
        LoginRequest request = new LoginRequest("user@pdp.local", "password123");
        when(authService.login(any(LoginRequest.class), any()))
                .thenReturn(new AuthResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void should_refresh_tokens() throws Exception {
        RefreshRequest request = new RefreshRequest("refresh-token");
        when(authService.refresh(any(RefreshRequest.class), any()))
                .thenReturn(new AuthResponse("new-access", "new-refresh"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void should_logout_user() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(TestExpectations.status(204));

        verify(authService).logout(any());
    }

    @Test
    void should_logout_all_devices() throws Exception {
        mockMvc.perform(post("/api/auth/logout-all"))
                .andExpect(TestExpectations.status(204));

        verify(authService).logoutAll(any());
    }

    @Test
    void should_reject_invalid_register_request() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(400));

        verify(authService, never()).register(any(RegisterRequest.class), any());
    }
}
