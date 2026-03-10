package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ProjectSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.entity.UserProject;
import com.datarain.pdp.signal.normalization.repository.UserProjectRepository;
import com.datarain.pdp.signal.normalization.service.ProjectNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectNormalizationServiceImpl implements ProjectNormalizationService {

    private final UserProjectRepository userProjectRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<ProjectSignal> projects = parsedSignal.projects();
        if (projects == null || projects.isEmpty()) {
            return 0;
        }

        List<UserProject> mapped = projects.stream()
                .map(project -> toEntity(signal, project))
                .toList();

        List<UserProject> deduped = dedupeBySourceHash(mapped);
        Set<String> hashes = deduped.stream()
                .map(UserProject::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : userProjectRepository.findExistingSourceHashes(hashes);

        List<UserProject> toSave = deduped.stream()
                .filter(project -> !existing.contains(project.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            userProjectRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private UserProject toEntity(MessageSignal signal, ProjectSignal projectSignal) {
        UserProject project = new UserProject();
        project.setUserId(signal.getUserId());
        project.setMessageId(signal.getMessageId());
        project.setProjectName(projectSignal.name());
        project.setNormalizedName(projectSignal.normalizedName());
        project.setSource(resolveSource(projectSignal.source()));
        project.setSignalId(signal.getId());
        project.setExtractionModel(signal.getExtractorModel());
        project.setPipelineVersion(signal.getPipelineVersion());
        project.setSourceHash(buildSourceHash(signal.getId(), projectSignal));
        return project;
    }

    private String buildSourceHash(UUID signalId, ProjectSignal projectSignal) {
        String payload = projectSignal.name() + "|" + projectSignal.normalizedName() + "|" + resolveSource(projectSignal.source());
        return sourceHashGenerator.hash(signalId, payload);
    }

    private String resolveSource(String source) {
        return source == null || source.isBlank() ? "llm" : source;
    }

    private List<UserProject> dedupeBySourceHash(List<UserProject> mapped) {
        if (mapped.isEmpty()) {
            return mapped;
        }
        return mapped.stream()
                .collect(Collectors.toMap(
                        UserProject::getSourceHash,
                        item -> item,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }
}
