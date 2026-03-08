package com.datarain.pdp.moderation.it;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Skeleton for moderation integration flow")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModerationCaseFlowMockMvcIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void should_require_auth_for_admin_moderation_cases_endpoint() throws Exception {
        mockMvc.perform(get("/admin/moderation/cases"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_create_manual_case_when_admin_authenticated() {
        // TODO: obtain admin token and call POST /admin/moderation/cases
        // TODO: assert response fields and initial status=PENDING
    }

    @Test
    void should_approve_case_and_write_audit_log() {
        // TODO: create case, call /approve, assert status transition and audit side effects
    }
}
