package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.dto.ToolSignal;
import com.datarain.pdp.signal.normalization.entity.UserTool;
import com.datarain.pdp.signal.normalization.repository.UserToolRepository;
import com.datarain.pdp.signal.normalization.service.ToolNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToolNormalizationServiceImpl implements ToolNormalizationService {

    private final UserToolRepository userToolRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<ToolSignal> tools = parsedSignal.tools();
        if (tools == null || tools.isEmpty()) {
            return 0;
        }

        List<UserTool> mapped = tools.stream()
                .map(tool -> toEntity(signal, tool))
                .toList();

        List<UserTool> deduped = dedupeBySourceHash(mapped);
        Set<String> hashes = deduped.stream()
                .map(UserTool::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : userToolRepository.findExistingSourceHashes(hashes);

        List<UserTool> toSave = deduped.stream()
                .filter(tool -> !existing.contains(tool.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            userToolRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private UserTool toEntity(MessageSignal signal, ToolSignal toolSignal) {
        UserTool tool = new UserTool();
        tool.setUserId(signal.getUserId());
        tool.setMessageId(signal.getMessageId());
        tool.setToolName(toolSignal.name());
        tool.setNormalizedName(toolSignal.normalizedName());
        tool.setSource(resolveSource(toolSignal.source()));
        tool.setSignalId(signal.getId());
        tool.setExtractionModel(signal.getExtractorModel());
        tool.setPipelineVersion(signal.getPipelineVersion());
        tool.setSourceHash(buildSourceHash(signal.getId(), toolSignal));
        return tool;
    }

    private String buildSourceHash(UUID signalId, ToolSignal toolSignal) {
        String payload = toolSignal.name() + "|" + toolSignal.normalizedName() + "|" + resolveSource(toolSignal.source());
        return sourceHashGenerator.hash(signalId, payload);
    }

    private String resolveSource(String source) {
        return source == null || source.isBlank() ? "llm" : source;
    }

    private List<UserTool> dedupeBySourceHash(List<UserTool> mapped) {
        if (mapped.isEmpty()) {
            return mapped;
        }
        return mapped.stream()
                .collect(Collectors.toMap(
                        UserTool::getSourceHash,
                        item -> item,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }
}
