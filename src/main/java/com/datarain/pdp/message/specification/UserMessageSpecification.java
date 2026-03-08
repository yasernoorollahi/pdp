package com.datarain.pdp.message.specification;

import com.datarain.pdp.message.entity.UserMessage;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class UserMessageSpecification {

    private UserMessageSpecification() {
    }

    public static Specification<UserMessage> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<UserMessage> hasProcessed(Boolean processed) {
        if (processed == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("processed"), processed);
    }

    public static Specification<UserMessage> messageDateFrom(LocalDate fromDate) {
        if (fromDate == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("messageDate"), fromDate);
    }

    public static Specification<UserMessage> messageDateTo(LocalDate toDate) {
        if (toDate == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("messageDate"), toDate);
    }
}
