package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.EntitySignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.entity.UserEntity;
import com.datarain.pdp.signal.normalization.repository.UserEntityRepository;
import com.datarain.pdp.signal.normalization.service.EntityNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntityNormalizationServiceImpl implements EntityNormalizationService {

    private final UserEntityRepository userEntityRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<EntitySignal> entities = parsedSignal.entities();
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        LocalDate signalDate = signal.getCreatedAt() == null
                ? null
                : signal.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();

        List<UserEntity> mapped = entities.stream()
                .map(entitySignal -> toEntity(signal, entitySignal, signalDate))
                .toList();

        Set<String> hashes = mapped.stream()
                .map(UserEntity::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : userEntityRepository.findExistingSourceHashes(hashes);

        List<UserEntity> toSave = mapped.stream()
                .filter(entity -> !existing.contains(entity.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            userEntityRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private UserEntity toEntity(MessageSignal signal, EntitySignal entitySignal, LocalDate signalDate) {
        UserEntity entity = new UserEntity();
        entity.setUserId(signal.getUserId());
        entity.setName(entitySignal.name());
        entity.setCanonicalName(entitySignal.canonicalName());
        entity.setEntityType(entitySignal.entityType());
        entity.setFirstSeen(signalDate);
        entity.setLastSeen(signalDate);
        entity.setMentionCount(1);
        entity.setConfidence(entitySignal.confidence());
        entity.setSourceSignalId(signal.getId());
        entity.setExtractionModel(signal.getExtractorModel());
        entity.setPipelineVersion(signal.getPipelineVersion());
        entity.setSourceHash(buildSourceHash(signal.getId(), entitySignal));
        return entity;
    }

    private String buildSourceHash(UUID signalId, EntitySignal entitySignal) {
        String payload = entitySignal.entityType() + "|" + entitySignal.name() + "|" + entitySignal.canonicalName();
        return sourceHashGenerator.hash(signalId, payload);
    }
}
