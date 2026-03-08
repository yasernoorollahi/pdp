package com.datarain.pdp.extraction.repository.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.datarain.pdp.exception.business.AiExtractionServiceErrorException;
import com.datarain.pdp.exception.business.AiExtractionServiceUnavailableException;
import com.datarain.pdp.exception.business.AiExtractionTimeoutException;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.repository.ExtractionRepository;
import com.datarain.pdp.extraction.repository.dto.AiExtractionResponse;
import com.datarain.pdp.infrastructure.external.ai.AiExtractionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

import static com.datarain.pdp.infrastructure.logging.TraceIdFilter.TRACE_ID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ExtractionRepositoryImpl implements ExtractionRepository {

    @Qualifier("aiExtractionRestTemplate")
    private final RestTemplate aiExtractionRestTemplate;
    private final AiExtractionProperties aiExtractionProperties;
    private final ObjectMapper objectMapper;

    @Override
    public AiExtractionResponse extractSignals(ExtractionRequest request) {
        return callEndpoint("/extract/signals", request, AiExtractionResponse.class);
    }

    @Override
    public JsonNode extractFacts(ExtractionRequest request) {
        return callEndpoint("/extract/facts", request, JsonNode.class);
    }

    @Override
    public JsonNode extractIntent(ExtractionRequest request) {
        return callEndpoint("/extract/intent", request, JsonNode.class);
    }

    @Override
    public JsonNode extractTone(ExtractionRequest request) {
        return callEndpoint("/extract/tone", request, JsonNode.class);
    }

    @Override
    public JsonNode extractContext(ExtractionRequest request) {
        return callEndpoint("/extract/context", request, JsonNode.class);
    }

    @Override
    public JsonNode extractCognitive(ExtractionRequest request) {
        return callEndpoint("/extract/cognitive", request, JsonNode.class);
    }

    @Override
    public JsonNode extractTopics(ExtractionRequest request) {
        return callEndpoint("/extract/topics", request, JsonNode.class);
    }

    @Override
    public JsonNode extractClassify(ExtractionRequest request) {
        return callEndpoint("/extract/classify", request, JsonNode.class);
    }

    private <T> T callEndpoint(String endpointPath, ExtractionRequest request, Class<T> responseType) {
        DownstreamExtractionRequest downstreamRequest = toDownstreamRequest(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        String traceId = MDC.get(TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            headers.set("X-Trace-Id", traceId);
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(downstreamRequest);
        } catch (JsonProcessingException ex) {
            throw new AiExtractionServiceErrorException("Failed to serialize extraction request payload");
        }

        HttpEntity<DownstreamExtractionRequest> entity = new HttpEntity<>(downstreamRequest, headers);
        String url = aiExtractionProperties.getBaseUrl() + endpointPath;

        try {
            log.atInfo()
                    .addKeyValue("event", "extraction.downstream.call")
                    .addKeyValue("url", url)
                    .addKeyValue("payloadLength", payload.length())
                    .addKeyValue("provider", downstreamRequest.provider())
                    .addKeyValue("model", downstreamRequest.model())
                    .log("Calling AI extraction service");

            ResponseEntity<T> response = aiExtractionRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    responseType
            );
            T body = response.getBody();
            if (body == null) {
                throw new AiExtractionServiceErrorException("AI extraction service returned empty body");
            }
            return body;
        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                throw new AiExtractionTimeoutException();
            }
            throw new AiExtractionServiceUnavailableException(ex.getMessage());
        } catch (HttpStatusCodeException ex) {
            log.warn("AI extraction downstream responded with status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status == null) {
                status = ex.getStatusCode().is4xxClientError() ? HttpStatus.BAD_REQUEST : HttpStatus.BAD_GATEWAY;
            }
            throw new AiExtractionServiceErrorException(
                    "AI extraction service returned status " + ex.getStatusCode().value()
                            + " body: " + ex.getResponseBodyAsString(),
                    status
            );
        } catch (RestClientException ex) {
            throw new AiExtractionServiceUnavailableException(ex.getMessage());
        }
    }

    private DownstreamExtractionRequest toDownstreamRequest(ExtractionRequest request) {
        String provider = StringUtils.hasText(request.provider())
                ? request.provider().trim()
                : aiExtractionProperties.getDefaultProvider();
        String model = StringUtils.hasText(request.model())
                ? request.model().trim()
                : aiExtractionProperties.getDefaultModel();

        return new DownstreamExtractionRequest(
                request.text(),
                provider,
                model
        );
    }

    private record DownstreamExtractionRequest(
            String text,
            String provider,
            String model
    ) {
    }
}
