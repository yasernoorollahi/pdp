package com.datarain.pdp.integration.admin;

import com.datarain.pdp.support.AbstractIT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@Disabled("Integration test skeleton - implement with seeded ROLE_ADMIN token and job log fixtures")
@AutoConfigureMockMvc
class AdminMonitoringControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnSystemOverview() {
        // TODO implement
    }
}
