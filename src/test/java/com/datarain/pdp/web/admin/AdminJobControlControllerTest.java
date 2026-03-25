package com.datarain.pdp.web.admin;

import com.datarain.pdp.admin.controller.AdminJobControlController;
import com.datarain.pdp.admin.dto.JobControlResponse;
import com.datarain.pdp.admin.dto.JobControlUpdateRequest;
import com.datarain.pdp.admin.dto.JobStatusResponse;
import com.datarain.pdp.admin.service.AdminJobControlService;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.support.TestExpectations;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AdminJobControlController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = AdminJobControlController.class)
class AdminJobControlControllerTest extends AbstractWebMvcTest {

    @MockBean
    private AdminJobControlService adminJobControlService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_get_job_control_status() throws Exception {
        JobControlResponse response = new JobControlResponse(
                true,
                null,
                List.of(new JobStatusResponse("UserMessageAnalysisJob", "desc", true, null, true)),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(adminJobControlService.getStatus()).thenReturn(response);

        mockMvc.perform(get("/api/admin/jobs"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.globalEnabled").value(true))
                .andExpect(jsonPath("$.jobs[0].jobKey").value("UserMessageAnalysisJob"));
    }

    @Test
    void should_update_job_control_status() throws Exception {
        JobControlResponse response = new JobControlResponse(
                false,
                false,
                List.of(new JobStatusResponse("UserMessageAnalysisJob", "desc", true, false, false)),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(adminJobControlService.update(any(JobControlUpdateRequest.class), any())).thenReturn(response);

        String payload = """
                {
                  "globalEnabled": false,
                  "jobs": [
                    {"jobKey": "UserMessageAnalysisJob", "enabled": false}
                  ]
                }
                """;

        mockMvc.perform(put("/api/admin/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.globalEnabled").value(false))
                .andExpect(jsonPath("$.jobs[0].effectiveEnabled").value(false));
    }
}
