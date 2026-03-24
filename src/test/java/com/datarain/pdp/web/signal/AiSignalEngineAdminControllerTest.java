package com.datarain.pdp.web.signal;

import com.datarain.pdp.signal.controller.AiSignalEngineAdminController;
import com.datarain.pdp.signal.dto.AiSignalEngineRunRequest;
import com.datarain.pdp.signal.dto.AiSignalEngineRunResponse;
import com.datarain.pdp.signal.dto.MessageSignalResponse;
import com.datarain.pdp.signal.service.AiSignalEngineService;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import com.datarain.pdp.support.TestExpectations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AiSignalEngineAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = AiSignalEngineAdminController.class)
@TestPropertySource(properties = {
        "jobs.ai-signal-engine.batch-size=7",
        "jobs.ai-signal-engine.max-retries=2",
        "pdp.ai.extraction.default-provider=test-provider",
        "pdp.ai.extraction.default-model=test-model"
})
class AiSignalEngineAdminControllerTest extends AbstractWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiSignalEngineService aiSignalEngineService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_run_with_defaults_when_body_missing() throws Exception {
        when(aiSignalEngineService.processPendingUsefulMessages(7, 2, "test-provider", "test-model"))
                .thenReturn(5L);

        mockMvc.perform(post("/api/admin/ai-signal-engine/run"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.processedCount").value(5));

        verify(aiSignalEngineService).processPendingUsefulMessages(7, 2, "test-provider", "test-model");
    }

    @Test
    void should_run_with_request_overrides() throws Exception {
        AiSignalEngineRunRequest request = new AiSignalEngineRunRequest(10, "custom", "model-x");
        when(aiSignalEngineService.processPendingUsefulMessages(10, 2, "custom", "model-x"))
                .thenReturn(3L);

        mockMvc.perform(post("/api/admin/ai-signal-engine/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.processedCount").value(3));

        verify(aiSignalEngineService).processPendingUsefulMessages(10, 2, "custom", "model-x");
    }

    @Test
    void should_list_signals() throws Exception {
        UUID userId = UUID.randomUUID();
        JsonNode signals = objectMapper.readTree("{\"key\":\"value\"}");
        MessageSignalResponse response = new MessageSignalResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                1,
                signals,
                "model",
                120,
                "pipeline",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(aiSignalEngineService.getSignals(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/admin/ai-signal-engine/signals")
                        .param("userId", userId.toString())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()));
    }
}
