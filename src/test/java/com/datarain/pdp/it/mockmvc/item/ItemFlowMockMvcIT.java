package com.datarain.pdp.it.mockmvc.item;

import com.datarain.pdp.support.AbstractIT;
import com.datarain.pdp.support.TestSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;

@AutoConfigureMockMvc
//@Transactional
@ActiveProfiles("test")
class ItemFlowMockMvcIT extends AbstractIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void context_loads() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void should_create_item_when_authenticated() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);

        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                 "title": "from test: title-1",
                 "type": "NOTE",
                 "content": "content-1",
                 "description": "description-1"
                }
            """))
                .andExpect(status().isCreated());
    }

}
