package com.datarain.pdp.moderation.service.impl;

import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.security.web.SecurityUtils;
import com.datarain.pdp.infrastructure.audit.BusinessEventService;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.moderation.dto.ModerationCaseActionRequest;
import com.datarain.pdp.moderation.dto.ModerationCaseCreateRequest;
import com.datarain.pdp.moderation.dto.ModerationCaseResponse;
import com.datarain.pdp.moderation.entity.ModerationCase;
import com.datarain.pdp.moderation.entity.ModerationSource;
import com.datarain.pdp.moderation.entity.ModerationStatus;
import com.datarain.pdp.moderation.exception.InvalidModerationTransitionException;
import com.datarain.pdp.moderation.exception.ModerationCaseNotFoundException;
import com.datarain.pdp.moderation.mapper.ModerationCaseMapper;
import com.datarain.pdp.moderation.repository.ModerationCaseRepository;
import com.datarain.pdp.moderation.service.ModerationCaseService;
import com.datarain.pdp.moderation.specification.ModerationCaseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModerationCaseServiceImpl implements ModerationCaseService {

    private final ModerationCaseRepository moderationCaseRepository;
    private final ModerationCaseMapper moderationCaseMapper;
    private final BusinessEventService businessEventService;
    private final PdpMetrics metrics;

    @Override
    public Page<ModerationCaseResponse> findAll(Pageable pageable, ModerationStatus status) {
        return moderationCaseRepository.findAll(ModerationCaseSpecification.hasStatus(status), pageable)
                .map(moderationCaseMapper::toResponse);
    }

    @Override
    public ModerationCaseResponse getById(UUID id) {
        return moderationCaseMapper.toResponse(findCase(id));
    }

    @Override
    @Transactional
    public ModerationCaseResponse createCase(ModerationCaseCreateRequest request) {
        ModerationCase moderationCase = moderationCaseMapper.toEntity(request);
        moderationCase.setStatus(ModerationStatus.PENDING);

        ModerationCase saved = moderationCaseRepository.save(moderationCase);
        metrics.getModerationCaseCreatedCounter().increment();

        log.atInfo()
                .addKeyValue("event", "moderation.case.created")
                .addKeyValue("caseId", saved.getId())
                .addKeyValue("targetType", saved.getTargetType())
                .addKeyValue("targetId", saved.getTargetId())
                .addKeyValue("source", saved.getSource())
                .addKeyValue("riskScore", saved.getRiskScore())
                .log("Moderation case created");

        Actor actor = currentActor();
        businessEventService.log(
                BusinessEventType.MODERATION_CASE_CREATED,
                actor.email,
                actor.userId,
                "Moderation case created: " + saved.getId(),
                true
        );

        return moderationCaseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ModerationCaseResponse approve(UUID id, ModerationCaseActionRequest request) {
        return changeStatus(id, ModerationStatus.APPROVED, request.comment(), BusinessEventType.MODERATION_CASE_APPROVED);
    }

    @Override
    @Transactional
    public ModerationCaseResponse reject(UUID id, ModerationCaseActionRequest request) {
        return changeStatus(id, ModerationStatus.REJECTED, request.comment(), BusinessEventType.MODERATION_CASE_REJECTED);
    }

    @Override
    @Transactional
    public ModerationCaseResponse autoBlock(UUID id, ModerationCaseActionRequest request) {
        ModerationCaseResponse response = changeStatus(
                id,
                ModerationStatus.AUTO_BLOCKED,
                request.comment(),
                BusinessEventType.MODERATION_CASE_AUTO_BLOCKED
        );
        metrics.getModerationCaseAutoBlockedCounter().increment();
        return response;
    }

    private ModerationCaseResponse changeStatus(UUID id,
                                                ModerationStatus newStatus,
                                                String comment,
                                                BusinessEventType eventType) {
        ModerationCase moderationCase = findCase(id);
        ensureTransitionAllowed(moderationCase, newStatus);

        Actor actor = currentActor();

        moderationCase.setStatus(newStatus);
        moderationCase.setComment(comment);
        moderationCase.setReviewedAt(Instant.now());
        moderationCase.setReviewedBy(actor.userId);

        ModerationCase saved = moderationCaseRepository.save(moderationCase);
        metrics.getModerationCaseStateTransitionCounter().increment();

        log.atInfo()
                .addKeyValue("event", "moderation.case.status.changed")
                .addKeyValue("caseId", saved.getId())
                .addKeyValue("newStatus", saved.getStatus())
                .addKeyValue("reviewedBy", saved.getReviewedBy())
                .addKeyValue("source", saved.getSource())
                .log("Moderation case status changed");

        businessEventService.log(
                eventType,
                actor.email,
                actor.userId,
                "Moderation status changed to " + newStatus + " for case " + saved.getId(),
                true
        );

        return moderationCaseMapper.toResponse(saved);
    }

    private ModerationCase findCase(UUID id) {
        return moderationCaseRepository.findById(id)
                .orElseThrow(() -> new ModerationCaseNotFoundException(id));
    }

    private void ensureTransitionAllowed(ModerationCase moderationCase, ModerationStatus targetStatus) {
        if (moderationCase.getStatus() != ModerationStatus.PENDING) {
            throw new InvalidModerationTransitionException(
                    moderationCase.getId(),
                    targetStatus.name(),
                    moderationCase.getStatus().name()
            );
        }
    }

    private Actor currentActor() {
        if (SecurityUtils.isAuthenticated()) {
            return new Actor(SecurityUtils.currentUserId(), SecurityUtils.currentUsername());
        }
        return new Actor(null, ModerationSource.SYSTEM.name().toLowerCase() + "@pdp.local");
    }

    private record Actor(UUID userId, String email) {
    }
}
