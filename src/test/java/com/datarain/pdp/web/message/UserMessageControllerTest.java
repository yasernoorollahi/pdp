package com.datarain.pdp.web.message;

import com.datarain.pdp.message.controller.UserMessageController;
import com.datarain.pdp.message.dto.UserMessageCreateRequest;
import com.datarain.pdp.message.dto.UserMessageProcessedRequest;
import com.datarain.pdp.message.dto.UserMessageResponse;
import com.datarain.pdp.message.dto.UserMessageUpdateRequest;
import com.datarain.pdp.message.service.UserMessageService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = UserMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = UserMessageController.class)
class UserMessageControllerTest extends AbstractWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserMessageService userMessageService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_create_message() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessageResponse response = sampleResponse(messageId, false, "hello");
        when(userMessageService.create(any(UserMessageCreateRequest.class))).thenReturn(response);

        UserMessageCreateRequest request = new UserMessageCreateRequest("hello", LocalDate.of(2024, 1, 1));

        mockMvc.perform(post("/api/user-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(201))
                .andExpect(jsonPath("$.id").value(messageId.toString()));
    }

    @Test
    void should_get_message_by_id() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessageResponse response = sampleResponse(messageId, false, "hello");
        when(userMessageService.getById(messageId)).thenReturn(response);

        mockMvc.perform(get("/api/user-messages/{id}", messageId))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.id").value(messageId.toString()));
    }

    @Test
    void should_update_message() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessageResponse response = sampleResponse(messageId, false, "updated");
        when(userMessageService.update(any(UUID.class), any(UserMessageUpdateRequest.class))).thenReturn(response);

        UserMessageUpdateRequest request = new UserMessageUpdateRequest("updated", LocalDate.of(2024, 1, 2));

        mockMvc.perform(put("/api/user-messages/{id}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.content").value("updated"));
    }

    @Test
    void should_set_processed_status() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessageResponse response = sampleResponse(messageId, true, "hello");
        when(userMessageService.setProcessed(any(UUID.class), any(UserMessageProcessedRequest.class)))
                .thenReturn(response);

        UserMessageProcessedRequest request = new UserMessageProcessedRequest(true);

        mockMvc.perform(patch("/api/user-messages/{id}/processed", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.processed").value(true));
    }

    @Test
    void should_delete_message() throws Exception {
        UUID messageId = UUID.randomUUID();

        mockMvc.perform(delete("/api/user-messages/{id}", messageId))
                .andExpect(TestExpectations.status(204));

        verify(userMessageService).delete(messageId);
    }

    @Test
    void should_list_messages() throws Exception {
        UUID messageId = UUID.randomUUID();
        UserMessageResponse response = sampleResponse(messageId, false, "hello");
        when(userMessageService.getAll(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/user-messages")
                        .param("processed", "false")
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-01-31")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.content[0].id").value(messageId.toString()));
    }

    @Test
    void should_reject_invalid_create_request() throws Exception {
        UserMessageCreateRequest request = new UserMessageCreateRequest("", LocalDate.of(2024, 1, 1));

        mockMvc.perform(post("/api/user-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(400));

        verify(userMessageService, never()).create(any(UserMessageCreateRequest.class));
    }

    private UserMessageResponse sampleResponse(UUID messageId, boolean processed, String content) {
        return new UserMessageResponse(
                messageId,
                UUID.randomUUID(),
                content,
                LocalDate.of(2024, 1, 1),
                processed,
                "USEFUL",
                "PENDING",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }
}
