package com.datarain.pdp.integration.signal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@Disabled("Integration test skeleton - implement with seeded ROLE_ADMIN token and extraction service")
@SpringBootTest
@AutoConfigureMockMvc
class AiSignalEngineAdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRunAiSignalEngineAsAdmin() {
        // TODO implement
    }

    @Test
    void shouldListSignalsWithPagination() {
        // TODO implement
    }
}
