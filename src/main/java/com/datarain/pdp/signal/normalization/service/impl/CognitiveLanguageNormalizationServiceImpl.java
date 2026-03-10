package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.CognitiveLanguageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.entity.CognitiveLanguageItem;
import com.datarain.pdp.signal.normalization.repository.CognitiveLanguageItemRepository;
import com.datarain.pdp.signal.normalization.service.CognitiveLanguageNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CognitiveLanguageNormalizationServiceImpl implements CognitiveLanguageNormalizationService {

    private final CognitiveLanguageItemRepository cognitiveLanguageItemRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<CognitiveLanguageSignal> languages = parsedSignal.cognitiveLanguages();
        if (languages == null || languages.isEmpty()) {
            return 0;
        }

        List<CognitiveLanguageItem> mapped = languages.stream()
                .map(lang -> toEntity(signal, lang))
                .toList();

        List<CognitiveLanguageItem> deduped = dedupeBySourceHash(mapped);
        Set<String> hashes = deduped.stream()
                .map(CognitiveLanguageItem::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : cognitiveLanguageItemRepository.findExistingSourceHashes(hashes);

        List<CognitiveLanguageItem> toSave = deduped.stream()
                .filter(item -> !existing.contains(item.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            cognitiveLanguageItemRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private CognitiveLanguageItem toEntity(MessageSignal signal, CognitiveLanguageSignal languageSignal) {
        CognitiveLanguageItem item = new CognitiveLanguageItem();
        item.setUserId(signal.getUserId());
        item.setMessageId(signal.getMessageId());
        item.setLanguageType(languageSignal.type());
        item.setValue(languageSignal.value());
        item.setSignalId(signal.getId());
        item.setExtractionModel(signal.getExtractorModel());
        item.setPipelineVersion(signal.getPipelineVersion());
        item.setSourceHash(buildSourceHash(signal.getId(), languageSignal));
        return item;
    }

    private String buildSourceHash(UUID signalId, CognitiveLanguageSignal languageSignal) {
        String payload = languageSignal.type() + "|" + languageSignal.value();
        return sourceHashGenerator.hash(signalId, payload);
    }

    private List<CognitiveLanguageItem> dedupeBySourceHash(List<CognitiveLanguageItem> mapped) {
        if (mapped.isEmpty()) {
            return mapped;
        }
        return mapped.stream()
                .collect(Collectors.toMap(
                        CognitiveLanguageItem::getSourceHash,
                        item -> item,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }
}
