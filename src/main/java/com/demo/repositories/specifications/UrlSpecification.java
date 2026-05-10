package com.demo.repositories.specifications;

import com.demo.entities.Url;
import com.demo.entities.enums.UrlStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UrlSpecification {
    public static Specification<Url> hasStatus(UrlStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Url> hasUserId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

}
