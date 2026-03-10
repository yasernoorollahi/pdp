package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.IntentSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.entity.IntentItem;
import com.datarain.pdp.signal.normalization.repository.IntentItemRepository;
import com.datarain.pdp.signal.normalization.service.IntentNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntentNormalizationServiceImpl implements IntentNormalizationService {

    private final IntentItemRepository intentItemRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<IntentSignal> intents = parsedSignal.intents();
        if (intents == null || intents.isEmpty()) {
            return 0;
        }

        List<IntentItem> mapped = intents.stream()
                .map(intent -> toEntity(signal, intent))
                .toList();

        Set<String> hashes = mapped.stream()
                .map(IntentItem::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : intentItemRepository.findExistingSourceHashes(hashes);

        List<IntentItem> toSave = mapped.stream()
                .filter(item -> !existing.contains(item.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            intentItemRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private IntentItem toEntity(MessageSignal signal, IntentSignal intentSignal) {
        IntentItem item = new IntentItem();
        item.setUserId(signal.getUserId());
        item.setMessageId(signal.getMessageId());
        item.setIntentType(intentSignal.intentType());
        item.setDescription(intentSignal.description());
        item.setTemporalScope(intentSignal.temporalScope());
        item.setSignalId(signal.getId());
        item.setExtractionModel(signal.getExtractorModel());
        item.setPipelineVersion(signal.getPipelineVersion());
        item.setSourceHash(buildSourceHash(signal.getId(), intentSignal));
        return item;
    }

    private String buildSourceHash(UUID signalId, IntentSignal intentSignal) {
        String payload = intentSignal.intentType() + "|" + intentSignal.description() + "|" + intentSignal.temporalScope();
        return sourceHashGenerator.hash(signalId, payload);
    }
}
