package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ActivitySignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.entity.UserActivity;
import com.datarain.pdp.signal.normalization.repository.UserActivityRepository;
import com.datarain.pdp.signal.normalization.service.ActivityNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityNormalizationServiceImpl implements ActivityNormalizationService {

    private final UserActivityRepository userActivityRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<ActivitySignal> activities = parsedSignal.activities();
        if (activities == null || activities.isEmpty()) {
            return 0;
        }

        List<UserActivity> mapped = activities.stream()
                .map(activity -> toEntity(signal, activity))
                .toList();

        List<UserActivity> deduped = dedupeBySourceHash(mapped);
        Set<String> hashes = deduped.stream()
                .map(UserActivity::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : userActivityRepository.findExistingSourceHashes(hashes);

        List<UserActivity> toSave = deduped.stream()
                .filter(activity -> !existing.contains(activity.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            userActivityRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private List<UserActivity> dedupeBySourceHash(List<UserActivity> mapped) {
        if (mapped.isEmpty()) {
            return mapped;
        }
        return mapped.stream()
                .collect(Collectors.toMap(
                        UserActivity::getSourceHash,
                        activity -> activity,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private UserActivity toEntity(MessageSignal signal, ActivitySignal activitySignal) {
        UserActivity activity = new UserActivity();
        activity.setUserId(signal.getUserId());
        activity.setMessageId(signal.getMessageId());
        activity.setActivityName(activitySignal.name());
        activity.setActivityDate(activitySignal.date());
        activity.setSignalId(signal.getId());
        activity.setExtractionModel(signal.getExtractorModel());
        activity.setPipelineVersion(signal.getPipelineVersion());
        activity.setSourceHash(buildSourceHash(signal.getId(), activitySignal));
        return activity;
    }

    private String buildSourceHash(UUID signalId, ActivitySignal activitySignal) {
        String payload = activitySignal.name() + "|" + activitySignal.date();
        return sourceHashGenerator.hash(signalId, payload);
    }
}
