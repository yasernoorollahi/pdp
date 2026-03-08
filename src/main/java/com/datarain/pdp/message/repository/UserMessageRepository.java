package com.datarain.pdp.message.repository;

import com.datarain.pdp.message.entity.UserMessage;
import com.datarain.pdp.message.entity.MessageAnalysisStatus;
import com.datarain.pdp.message.entity.MessageProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UserMessageRepository extends JpaRepository<UserMessage, UUID>, JpaSpecificationExecutor<UserMessage> {

    Optional<UserMessage> findByIdAndUserId(UUID id, UUID userId);

    Page<UserMessage> findByAnalysisStatusOrderByCreatedAtAsc(MessageAnalysisStatus analysisStatus, Pageable pageable);

    @Query("""
            SELECT m
            FROM UserMessage m
            WHERE (
                UPPER(TRIM(COALESCE(m.signalDecision, ''))) = 'USEFUL'
                AND m.analysisStatus = com.datarain.pdp.message.entity.MessageAnalysisStatus.ANALYZED
            )
              AND m.processingStatus IN :statuses
              AND m.retryCount < :maxRetries
            ORDER BY m.createdAt ASC
            """)
    Page<UserMessage> findProcessableUsefulMessages(@Param("statuses") Collection<MessageProcessingStatus> statuses,
                                                    @Param("maxRetries") int maxRetries,
                                                    Pageable pageable);
}
