package com.datarain.pdp.it.mockmvc.extraction;

import com.datarain.pdp.support.TestSecurity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.datarain.pdp.support.AbstractIT;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExtractionFlowMockMvcIT extends AbstractIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void should_require_authentication() throws Exception {
        mockMvc.perform(post("/api/extraction/signals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "hello"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_extract_signals_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);

        mockMvc.perform(post("/api/extraction/signals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "I felt anxious after the meeting and need help.",
                                  "provider": "ollama",
                                  "model": "qwen2.5:7b"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_extract_facts_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);
        performExtraction("/api/extraction/facts", token);
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_extract_intent_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);
        performExtraction("/api/extraction/intent", token);
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_extract_tone_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);
        performExtraction("/api/extraction/tone", token);
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_extract_context_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);
        performExtraction("/api/extraction/context", token);
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_extract_cognitive_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);
        performExtraction("/api/extraction/cognitive", token);
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_extract_topics_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);
        performExtraction("/api/extraction/topics", token);
    }

    @Test
    @Disabled("Skeleton only: requires running extraction service on localhost:3000")
    void should_support_legacy_extract_endpoint_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);
        performExtraction("/api/extraction/extract", token);
    }

    @Test
    void should_forbid_classify_for_admin() throws Exception {
        String token = TestSecurity.adminToken(mockMvc);

        mockMvc.perform(post("/api/extraction/classify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "I felt anxious after the meeting and need help.",
                                  "provider": "ollama",
                                  "model": "qwen2.5:7b"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @Disabled("Skeleton only: requires system-role token and running extraction service on localhost:3000")
    void should_extract_classify_for_system() throws Exception {
        String systemToken = "replace-with-system-token";
        performExtraction("/api/extraction/classify", systemToken);
    }

    private void performExtraction(String path, String token) throws Exception {
        mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "I felt anxious after the meeting and need help.",
                                  "provider": "ollama",
                                  "model": "qwen2.5:7b"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
