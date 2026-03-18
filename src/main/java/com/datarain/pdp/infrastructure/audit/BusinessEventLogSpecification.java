package com.datarain.pdp.infrastructure.audit;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BusinessEventLogSpecification {

    private BusinessEventLogSpecification() {
    }

    public static Specification<BusinessEventLog> filter(UUID userId,
                                                         String email,
                                                         BusinessEventType eventType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (email != null && !email.isBlank()) {
                predicates.add(cb.equal(root.get("email"), email.trim()));
            }
            if (eventType != null) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
