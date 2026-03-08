package com.datarain.pdp.moderation.specification;

import com.datarain.pdp.moderation.entity.ModerationCase;
import com.datarain.pdp.moderation.entity.ModerationStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ModerationCaseSpecification {

    private ModerationCaseSpecification() {
    }

    public static Specification<ModerationCase> hasStatus(ModerationStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
}
