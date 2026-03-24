package com.datarain.pdp.web.user;

import com.datarain.pdp.infrastructure.security.Role;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.user.controller.UserController;
import com.datarain.pdp.user.dto.UserResponse;
import com.datarain.pdp.user.service.UserService;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import com.datarain.pdp.support.TestExpectations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = UserController.class)
class UserControllerTest extends AbstractWebMvcTest {

    @MockBean
    private UserService userService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_list_users() throws Exception {
        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "user@pdp.local",
                Set.of(Role.ROLE_USER),
                true,
                false,
                0,
                false,
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(userService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.content[0].email").value("user@pdp.local"));
    }

    @Test
    void should_get_current_user() throws Exception {
        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "me@pdp.local",
                Set.of(Role.ROLE_USER),
                true,
                false,
                0,
                false,
                Instant.parse("2024-01-02T00:00:00Z")
        );
        when(userService.me()).thenReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.email").value("me@pdp.local"));
    }

    @Test
    void should_get_user_by_id() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(
                userId,
                "target@pdp.local",
                Set.of(Role.ROLE_ADMIN),
                true,
                true,
                1,
                false,
                Instant.parse("2024-01-03T00:00:00Z")
        );
        when(userService.getById(userId)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.email").value("target@pdp.local"));
    }

    @Test
    void should_set_user_enabled_state() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{id}/enabled", userId)
                        .param("enabled", "true"))
                .andExpect(TestExpectations.status(204));

        verify(userService).setEnabled(userId, true);
    }

    @Test
    void should_unlock_user() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/users/{id}/unlock", userId))
                .andExpect(TestExpectations.status(204));

        verify(userService).unlock(userId);
    }
}
