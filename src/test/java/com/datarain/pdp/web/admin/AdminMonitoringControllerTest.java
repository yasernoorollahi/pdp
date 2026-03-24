package com.datarain.pdp.web.admin;

import com.datarain.pdp.admin.controller.AdminMonitoringController;
import com.datarain.pdp.admin.dto.AdminSystemOverviewResponse;
import com.datarain.pdp.admin.dto.BusinessStatsResponse;
import com.datarain.pdp.admin.dto.JobExecutionLogResponse;
import com.datarain.pdp.admin.dto.SystemOverviewResponse;
import com.datarain.pdp.admin.service.AdminMonitoringService;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionStatus;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import com.datarain.pdp.support.TestExpectations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AdminMonitoringController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = AdminMonitoringController.class)
class AdminMonitoringControllerTest extends AbstractWebMvcTest {

    @MockBean
    private AdminMonitoringService adminMonitoringService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_get_system_overview() throws Exception {
        AdminSystemOverviewResponse response = new AdminSystemOverviewResponse(
                new BusinessStatsResponse(10, 9, 1, 20, 18, 2),
                new SystemOverviewResponse(
                        "UP",
                        Map.of("db", "UP"),
                        new SystemOverviewResponse.MetricSnapshot(
                                1, 2, 3, 0.1, 0.2, 120, 200, 0.3,
                                new SystemOverviewResponse.HikariSnapshot(1, 2, 0, 10, 1)
                        ),
                        Instant.parse("2024-01-01T00:00:00Z")
                ),
                List.of(new JobExecutionLogResponse(
                        UUID.randomUUID(),
                        "job",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        Instant.parse("2024-01-01T00:01:00Z"),
                        JobExecutionStatus.SUCCESS,
                        60,
                        5,
                        null
                )),
                Instant.parse("2024-01-01T00:02:00Z")
        );
        when(adminMonitoringService.getSystemOverview(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/admin/system-overview").param("jobsLimit", "10"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.businessStats.totalUsers").value(10))
                .andExpect(jsonPath("$.system.overallStatus").value("UP"));
    }

    @Test
    void should_reject_invalid_jobs_limit() throws Exception {
        mockMvc.perform(get("/api/admin/system-overview").param("jobsLimit", "0"))
                .andExpect(TestExpectations.status(400));

        verify(adminMonitoringService, never()).getSystemOverview(any(), any());
    }
}
