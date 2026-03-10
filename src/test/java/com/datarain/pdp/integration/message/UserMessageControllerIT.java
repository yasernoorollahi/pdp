package com.datarain.pdp.integration.message;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.datarain.pdp.support.AbstractIT;
import org.springframework.test.web.servlet.MockMvc;

@Disabled("Integration test skeleton - implement with seeded ROLE_USER token")
@AutoConfigureMockMvc
class UserMessageControllerIT extends AbstractIT {

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
