package com.datarain.pdp.signal.repository;

import com.datarain.pdp.signal.entity.MessageSignal;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageSignalRepository extends JpaRepository<MessageSignal, UUID> {

    Optional<MessageSignal> findTopByMessageIdOrderBySignalVersionDesc(UUID messageId);

    Page<MessageSignal> findAllByUserId(UUID userId, Pageable pageable);

    @Query(value = """
            SELECT *
            FROM message_signals
            WHERE COALESCE(normalized, false) = false
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<MessageSignal> findBatchForNormalization(@Param("batchSize") int batchSize);

    @Modifying
    @Query(value = """
            UPDATE message_signals
            SET normalization_started_at = :startedAt
            WHERE id IN (:ids)
            """, nativeQuery = true)
    int markNormalizationStarted(@Param("ids") List<UUID> ids, @Param("startedAt") Instant startedAt);

    @Modifying
    @Query(value = """
            UPDATE message_signals
            SET normalized = true,
                normalized_at = :normalizedAt,
                normalization_version = :version
            WHERE id = :id
            """, nativeQuery = true)
    int markNormalized(@Param("id") UUID id,
                       @Param("normalizedAt") Instant normalizedAt,
                       @Param("version") int version);
}
