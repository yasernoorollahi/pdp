package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.CognitiveSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.entity.CognitiveState;
import com.datarain.pdp.signal.normalization.repository.CognitiveStateRepository;
import com.datarain.pdp.signal.normalization.service.CognitiveNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CognitiveNormalizationServiceImpl implements CognitiveNormalizationService {

    private final CognitiveStateRepository cognitiveStateRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        CognitiveSignal cognitive = parsedSignal.cognitive();
        if (cognitive == null || (cognitive.clarityLevel() == null && cognitive.decisionState() == null
                && cognitive.hesitationDetected() == null)) {
            return 0;
        }

        CognitiveState state = toEntity(signal, cognitive);
        String hash = state.getSourceHash();

        Set<String> existing = hash == null
                ? Set.of()
                : cognitiveStateRepository.findExistingSourceHashes(Set.of(hash));

        if (existing.contains(hash)) {
            return 0;
        }

        cognitiveStateRepository.save(state);
        return 1;
    }

    private CognitiveState toEntity(MessageSignal signal, CognitiveSignal cognitiveSignal) {
        CognitiveState state = new CognitiveState();
        state.setUserId(signal.getUserId());
        state.setMessageId(signal.getMessageId());
        state.setClarityLevel(cognitiveSignal.clarityLevel());
        state.setDecisionState(cognitiveSignal.decisionState());
        state.setHesitationDetected(cognitiveSignal.hesitationDetected());
        state.setSignalId(signal.getId());
        state.setExtractionModel(signal.getExtractorModel());
        state.setPipelineVersion(signal.getPipelineVersion());
        state.setSourceHash(buildSourceHash(signal.getId(), cognitiveSignal));
        return state;
    }

    private String buildSourceHash(UUID signalId, CognitiveSignal cognitiveSignal) {
        String payload = cognitiveSignal.clarityLevel() + "|" + cognitiveSignal.decisionState() + "|" + cognitiveSignal.hesitationDetected();
        return sourceHashGenerator.hash(signalId, payload);
    }
}
