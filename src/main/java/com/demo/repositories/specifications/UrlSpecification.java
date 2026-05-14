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

    public static Specification<Url> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String lowerKeyword = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("baseUrl")), lowerKeyword),
                    cb.like(cb.lower(root.get("shortCode")), lowerKeyword)
            );
        };
    }

}
