package com.datarain.pdp.integration.message;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@Disabled("Integration test skeleton - implement with seeded ROLE_USER token")
@SpringBootTest
@AutoConfigureMockMvc
class UserMessageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUserMessage() {
        // TODO implement
    }

    @Test
    void shouldListCurrentUserMessagesWithPagination() {
        // TODO implement
    }

    @Test
    void shouldUpdateAndDeleteCurrentUserMessage() {
        // TODO implement
    }
}
