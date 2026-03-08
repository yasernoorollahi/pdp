package com.datarain.pdp.signal.repository;

import com.datarain.pdp.signal.entity.MessageSignal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageSignalRepository extends JpaRepository<MessageSignal, UUID> {

    Optional<MessageSignal> findTopByMessageIdOrderBySignalVersionDesc(UUID messageId);

    Page<MessageSignal> findAllByUserId(UUID userId, Pageable pageable);
}
