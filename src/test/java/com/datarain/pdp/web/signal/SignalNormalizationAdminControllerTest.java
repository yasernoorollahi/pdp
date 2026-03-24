package com.datarain.pdp.web.signal;

import com.datarain.pdp.signal.normalization.controller.SignalNormalizationAdminController;
import com.datarain.pdp.signal.normalization.dto.SignalNormalizationRunRequest;
import com.datarain.pdp.signal.normalization.dto.SignalNormalizationRunResponse;
import com.datarain.pdp.signal.normalization.service.SignalNormalizationService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = SignalNormalizationAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SignalNormalizationAdminController.class)
@TestPropertySource(properties = {
        "jobs.signal-normalization.batch-size=12"
})
class SignalNormalizationAdminControllerTest extends AbstractWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SignalNormalizationService signalNormalizationService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_run_with_defaults_when_body_missing() throws Exception {
        when(signalNormalizationService.processPendingSignals(12)).thenReturn(4L);

        mockMvc.perform(post("/api/admin/signal-normalization/run"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.processedCount").value(4));

        verify(signalNormalizationService).processPendingSignals(12);
    }

    @Test
    void should_run_with_custom_batch_size() throws Exception {
        SignalNormalizationRunRequest request = new SignalNormalizationRunRequest(5);
        when(signalNormalizationService.processPendingSignals(5)).thenReturn(2L);

        mockMvc.perform(post("/api/admin/signal-normalization/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.processedCount").value(2));

        verify(signalNormalizationService).processPendingSignals(5);
    }
}
