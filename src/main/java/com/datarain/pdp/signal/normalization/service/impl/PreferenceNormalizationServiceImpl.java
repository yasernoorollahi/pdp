package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.dto.PreferenceSignal;
import com.datarain.pdp.signal.normalization.entity.UserPreference;
import com.datarain.pdp.signal.normalization.repository.UserPreferenceRepository;
import com.datarain.pdp.signal.normalization.service.PreferenceNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenceNormalizationServiceImpl implements PreferenceNormalizationService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<PreferenceSignal> preferences = parsedSignal.preferences();
        if (preferences == null || preferences.isEmpty()) {
            return 0;
        }

        List<UserPreference> mapped = preferences.stream()
                .map(pref -> toEntity(signal, pref))
                .toList();

        Set<String> hashes = mapped.stream()
                .map(UserPreference::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : userPreferenceRepository.findExistingSourceHashes(hashes);

        List<UserPreference> toSave = mapped.stream()
                .filter(pref -> !existing.contains(pref.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            userPreferenceRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private UserPreference toEntity(MessageSignal signal, PreferenceSignal preferenceSignal) {
        UserPreference preference = new UserPreference();
        preference.setUserId(signal.getUserId());
        preference.setMessageId(signal.getMessageId());
        preference.setPreferenceType(preferenceSignal.preferenceType());
        preference.setValue(preferenceSignal.value());
        preference.setSignalId(signal.getId());
        preference.setExtractionModel(signal.getExtractorModel());
        preference.setPipelineVersion(signal.getPipelineVersion());
        preference.setSourceHash(buildSourceHash(signal.getId(), preferenceSignal));
        return preference;
    }

    private String buildSourceHash(UUID signalId, PreferenceSignal preferenceSignal) {
        String payload = preferenceSignal.preferenceType() + "|" + preferenceSignal.value();
        return sourceHashGenerator.hash(signalId, payload);
    }
}
