package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ContextSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.entity.UserContextFlag;
import com.datarain.pdp.signal.normalization.repository.UserContextFlagRepository;
import com.datarain.pdp.signal.normalization.service.ContextNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContextNormalizationServiceImpl implements ContextNormalizationService {

    private final UserContextFlagRepository userContextFlagRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        ContextSignal context = parsedSignal.context();
        if (context == null || context.collaborationDetected() == null) {
            return 0;
        }

        UserContextFlag flag = toEntity(signal, context);
        String hash = flag.getSourceHash();
        Set<String> existing = hash == null
                ? Set.of()
                : userContextFlagRepository.findExistingSourceHashes(Set.of(hash));

        if (existing.contains(hash)) {
            return 0;
        }

        userContextFlagRepository.save(flag);
        return 1;
    }

    private UserContextFlag toEntity(MessageSignal signal, ContextSignal contextSignal) {
        UserContextFlag flag = new UserContextFlag();
        flag.setUserId(signal.getUserId());
        flag.setMessageId(signal.getMessageId());
        flag.setCollaborationDetected(contextSignal.collaborationDetected());
        flag.setSignalId(signal.getId());
        flag.setExtractionModel(signal.getExtractorModel());
        flag.setPipelineVersion(signal.getPipelineVersion());
        flag.setSourceHash(buildSourceHash(signal.getId(), contextSignal));
        return flag;
    }

    private String buildSourceHash(UUID signalId, ContextSignal contextSignal) {
        String payload = String.valueOf(contextSignal.collaborationDetected());
        return sourceHashGenerator.hash(signalId, payload);
    }
}
