package com.datarain.pdp.web.extraction;

import com.datarain.pdp.extraction.controller.ExtractionController;
import com.datarain.pdp.extraction.dto.ClassifyExtractionResponse;
import com.datarain.pdp.extraction.dto.CognitiveExtractionResponse;
import com.datarain.pdp.extraction.dto.ContextExtractionResponse;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.dto.ExtractionResponse;
import com.datarain.pdp.extraction.dto.FactsExtractionResponse;
import com.datarain.pdp.extraction.dto.IntentExtractionResponse;
import com.datarain.pdp.extraction.dto.ToneExtractionResponse;
import com.datarain.pdp.extraction.dto.TopicsExtractionResponse;
import com.datarain.pdp.extraction.service.ExtractionService;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import com.datarain.pdp.support.TestExpectations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ExtractionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ExtractionController.class)
class ExtractionControllerTest extends AbstractWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExtractionService extractionService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_extract_full() throws Exception {
        when(extractionService.extract(any(ExtractionRequest.class))).thenReturn(sampleExtractionResponse());

        mockMvc.perform(post("/api/extraction/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.overallSentiment").value("positive"));
    }

    @Test
    void should_extract_signals() throws Exception {
        when(extractionService.extractSignals(any(ExtractionRequest.class))).thenReturn(sampleExtractionResponse());

        mockMvc.perform(post("/api/extraction/signals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.confidence").value(0.9));
    }

    @Test
    void should_extract_facts() throws Exception {
        when(extractionService.extractFacts(any(ExtractionRequest.class)))
                .thenReturn(new FactsExtractionResponse(sampleJson("facts")));

        mockMvc.perform(post("/api/extraction/facts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.facts.value").value("facts"));
    }

    @Test
    void should_extract_intent() throws Exception {
        when(extractionService.extractIntent(any(ExtractionRequest.class)))
                .thenReturn(new IntentExtractionResponse(sampleJson("intent")));

        mockMvc.perform(post("/api/extraction/intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.intent.value").value("intent"));
    }

    @Test
    void should_extract_tone() throws Exception {
        when(extractionService.extractTone(any(ExtractionRequest.class)))
                .thenReturn(new ToneExtractionResponse(sampleJson("tone")));

        mockMvc.perform(post("/api/extraction/tone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.tone.value").value("tone"));
    }

    @Test
    void should_extract_context() throws Exception {
        when(extractionService.extractContext(any(ExtractionRequest.class)))
                .thenReturn(new ContextExtractionResponse(sampleJson("context")));

        mockMvc.perform(post("/api/extraction/context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.context.value").value("context"));
    }

    @Test
    void should_extract_cognitive() throws Exception {
        when(extractionService.extractCognitive(any(ExtractionRequest.class)))
                .thenReturn(new CognitiveExtractionResponse(sampleJson("cognitive")));

        mockMvc.perform(post("/api/extraction/cognitive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.cognitive.value").value("cognitive"));
    }

    @Test
    void should_extract_topics() throws Exception {
        when(extractionService.extractTopics(any(ExtractionRequest.class)))
                .thenReturn(new TopicsExtractionResponse(sampleJson("topics")));

        mockMvc.perform(post("/api/extraction/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.topics.value").value("topics"));
    }

    @Test
    void should_classify_message() throws Exception {
        when(extractionService.extractClassify(any(ExtractionRequest.class)))
                .thenReturn(new ClassifyExtractionResponse(sampleJson("classify")));

        mockMvc.perform(post("/api/extraction/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.classify.value").value("classify"));
    }

    @Test
    void should_reject_invalid_request() throws Exception {
        String invalidBody = """
                {
                  "text": "",
                  "provider": "p",
                  "model": "m"
                }
                """;

        mockMvc.perform(post("/api/extraction/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(TestExpectations.status(400));

        verify(extractionService, never()).extract(any(ExtractionRequest.class));
    }

    private String validRequestBody() throws Exception {
        return objectMapper.writeValueAsString(new ExtractionRequest("hello", "provider", "model"));
    }

    private ExtractionResponse sampleExtractionResponse() {
        return new ExtractionResponse(
                List.of(new ExtractionResponse.EventItem(
                        "event",
                        "category",
                        "evidence",
                        List.of("actor"),
                        "location",
                        "today",
                        "positive",
                        0.8
                )),
                List.of(new ExtractionResponse.EmotionItem(
                        "joy",
                        "joy",
                        0.7,
                        0.3,
                        0.5,
                        "evidence",
                        0.9
                )),
                List.of(new ExtractionResponse.IntentItem(
                        "intent",
                        "category",
                        "evidence",
                        0.4,
                        0.9
                )),
                0.9,
                "positive",
                "joy",
                "intent",
                new ExtractionResponse.MetadataItem("en", 5, List.of("note"))
        );
    }

    private JsonNode sampleJson(String value) throws Exception {
        return objectMapper.readTree("{\"value\":\"" + value + "\"}");
    }
}
