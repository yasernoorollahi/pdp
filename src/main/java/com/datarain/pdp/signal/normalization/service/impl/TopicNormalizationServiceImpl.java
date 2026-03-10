package com.datarain.pdp.signal.normalization.service.impl;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.dto.TopicSignal;
import com.datarain.pdp.signal.normalization.entity.UserTopic;
import com.datarain.pdp.signal.normalization.repository.UserTopicRepository;
import com.datarain.pdp.signal.normalization.service.TopicNormalizationService;
import com.datarain.pdp.signal.normalization.util.SourceHashGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicNormalizationServiceImpl implements TopicNormalizationService {

    private final UserTopicRepository userTopicRepository;
    private final SourceHashGenerator sourceHashGenerator;

    @Override
    public int normalize(MessageSignal signal, ParsedSignal parsedSignal) {
        List<TopicSignal> topics = parsedSignal.topics();
        if (topics == null || topics.isEmpty()) {
            return 0;
        }

        List<UserTopic> mapped = topics.stream()
                .map(topic -> toEntity(signal, topic))
                .toList();

        Set<String> hashes = mapped.stream()
                .map(UserTopic::getSourceHash)
                .collect(Collectors.toSet());

        Set<String> existing = hashes.isEmpty()
                ? Set.of()
                : userTopicRepository.findExistingSourceHashes(hashes);

        List<UserTopic> toSave = mapped.stream()
                .filter(topic -> !existing.contains(topic.getSourceHash()))
                .toList();

        if (!toSave.isEmpty()) {
            userTopicRepository.saveAll(toSave);
        }

        return toSave.size();
    }

    private UserTopic toEntity(MessageSignal signal, TopicSignal topicSignal) {
        UserTopic topic = new UserTopic();
        topic.setUserId(signal.getUserId());
        topic.setMessageId(signal.getMessageId());
        topic.setTopic(topicSignal.topic());
        topic.setDomain(topicSignal.domain());
        topic.setSignalId(signal.getId());
        topic.setExtractionModel(signal.getExtractorModel());
        topic.setPipelineVersion(signal.getPipelineVersion());
        topic.setSourceHash(buildSourceHash(signal.getId(), topicSignal));
        return topic;
    }

    private String buildSourceHash(UUID signalId, TopicSignal topicSignal) {
        String payload = topicSignal.topic() + "|" + topicSignal.domain();
        return sourceHashGenerator.hash(signalId, payload);
    }
}
