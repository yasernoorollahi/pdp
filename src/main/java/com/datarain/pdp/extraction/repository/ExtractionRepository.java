package com.datarain.pdp.extraction.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.repository.dto.AiExtractionResponse;

public interface ExtractionRepository {

    AiExtractionResponse extractSignals(ExtractionRequest request);

    JsonNode extractFacts(ExtractionRequest request);

    JsonNode extractIntent(ExtractionRequest request);

    JsonNode extractTone(ExtractionRequest request);

    JsonNode extractContext(ExtractionRequest request);

    JsonNode extractCognitive(ExtractionRequest request);

    JsonNode extractTopics(ExtractionRequest request);

    JsonNode extractClassify(ExtractionRequest request);
}
