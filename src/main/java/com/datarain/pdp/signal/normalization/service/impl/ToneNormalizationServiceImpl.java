package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.dto.ToneSignal;
import com.datarain.pdp.signal.normalization.entity.ToneState;
import com.datarain.pdp.signal.normalization.repository.ToneStateRepository;
import com.datarain.pdp.signal.normalization.service.ToneNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ToneNormalizationServiceImpl implements ToneNormalizationService {

    private final ToneStateRepository toneStateRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        ToneSignal tone = parsedSignal.tone();
        if (tone == null || (tone.sentiment() == null && tone.mood() == null
                && tone.motivationLevel() == null && tone.effortPerception() == null
                && tone.frictionDetected() == null)) {
            return 0;
        }

        ToneState state = toEntity(signal, tone);
        String hash = state.getSourceHash();
        Set<String> existing = hash == null
                ? Set.of()
                : toneStateRepository.findExistingSourceHashes(Set.of(hash));

        if (existing.contains(hash)) {
            return 0;
        }

        toneStateRepository.save(state);
        return 1;
    }

    private ToneState toEntity(MessageSignal signal, ToneSignal toneSignal) {
        ToneState state = new ToneState();
        state.setUserId(signal.getUserId());
        state.setMessageId(signal.getMessageId());
        state.setSentiment(toneSignal.sentiment());
        state.setMood(toneSignal.mood());
        state.setMotivationLevel(toneSignal.motivationLevel());
        state.setEffortPerception(toneSignal.effortPerception());
        state.setFrictionDetected(toneSignal.frictionDetected());
        state.setSignalId(signal.getId());
        state.setExtractionModel(signal.getExtractorModel());
        state.setPipelineVersion(signal.getPipelineVersion());
        state.setSourceHash(buildSourceHash(signal.getId(), toneSignal));
        return state;
    }

    private String buildSourceHash(UUID signalId, ToneSignal toneSignal) {
        String payload = toneSignal.sentiment() + "|" + toneSignal.mood() + "|"
                + toneSignal.motivationLevel() + "|" + toneSignal.effortPerception()
                + "|" + toneSignal.frictionDetected();
        return sourceHashGenerator.hash(signalId, payload);
    }
}
