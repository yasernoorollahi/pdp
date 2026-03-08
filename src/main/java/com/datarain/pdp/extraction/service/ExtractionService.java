package com.datarain.pdp.extraction.service;

import com.datarain.pdp.extraction.dto.CognitiveExtractionResponse;
import com.datarain.pdp.extraction.dto.ClassifyExtractionResponse;
import com.datarain.pdp.extraction.dto.ContextExtractionResponse;
import com.datarain.pdp.extraction.dto.ExtractionRequest;
import com.datarain.pdp.extraction.dto.ExtractionResponse;
import com.datarain.pdp.extraction.dto.FactsExtractionResponse;
import com.datarain.pdp.extraction.dto.IntentExtractionResponse;
import com.datarain.pdp.extraction.dto.ToneExtractionResponse;
import com.datarain.pdp.extraction.dto.TopicsExtractionResponse;

public interface ExtractionService {

    ExtractionResponse extract(ExtractionRequest request);

    ExtractionResponse extractSignals(ExtractionRequest request);

    FactsExtractionResponse extractFacts(ExtractionRequest request);

    IntentExtractionResponse extractIntent(ExtractionRequest request);

    ToneExtractionResponse extractTone(ExtractionRequest request);

    ContextExtractionResponse extractContext(ExtractionRequest request);

    CognitiveExtractionResponse extractCognitive(ExtractionRequest request);

    TopicsExtractionResponse extractTopics(ExtractionRequest request);

    ClassifyExtractionResponse extractClassify(ExtractionRequest request);
}
