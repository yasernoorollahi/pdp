package com.datarain.pdp.it.mockmvc.auth;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.datarain.pdp.support.AbstractIT;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.datarain.pdp.web.support.ColorPrintingResultHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

import java.util.UUID;

@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowMockMvcIT extends AbstractIT {

    @Autowired
    MockMvc mockMvc;


    @Test
    void should_register_user_successfully() throws Exception {

        String email = "user-" + UUID.randomUUID() + "@pdp.local";

        String payload = """
        {
          "email": "%s",
          "password": "password123"
        }
        """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andDo(new ColorPrintingResultHandler())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }




    @Test
    void should_fail_when_email_already_exists() throws Exception {

        String email = "user-" + UUID.randomUUID() + "@pdp.local";

        String payload = """
            {
              "email": "%s",
              "password": "password123"
            }
            """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andDo(new ColorPrintingResultHandler())
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andDo(new ColorPrintingResultHandler())
                .andExpect(status().isConflict());
    }


}
