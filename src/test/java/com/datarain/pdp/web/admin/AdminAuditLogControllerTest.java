package com.datarain.pdp.web.admin;

import com.datarain.pdp.admin.controller.AdminAuditLogController;
import com.datarain.pdp.admin.dto.BusinessEventLogResponse;
import com.datarain.pdp.admin.dto.SecurityAuditLogResponse;
import com.datarain.pdp.admin.service.AdminAuditLogService;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import com.datarain.pdp.support.TestExpectations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AdminAuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = AdminAuditLogController.class)
class AdminAuditLogControllerTest extends AbstractWebMvcTest {

    @MockBean
    private AdminAuditLogService adminAuditLogService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_list_security_audit_logs() throws Exception {
        SecurityAuditLogResponse response = new SecurityAuditLogResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "user@pdp.local",
                SecurityEventType.LOGIN_SUCCESS,
                "127.0.0.1",
                "agent",
                "details",
                Instant.parse("2024-01-01T00:00:00Z"),
                true
        );
        when(adminAuditLogService.getSecurityAuditLogs(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/admin/audit/security")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.content[0].eventType").value("LOGIN_SUCCESS"));
    }

    @Test
    void should_list_business_event_logs() throws Exception {
        BusinessEventLogResponse response = new BusinessEventLogResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "user@pdp.local",
                BusinessEventType.USER_MESSAGE_CREATED,
                "details",
                Instant.parse("2024-01-01T00:00:00Z"),
                true
        );
        when(adminAuditLogService.getBusinessEventLogs(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/admin/audit/business")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.content[0].eventType").value("USER_MESSAGE_CREATED"));
    }
}
