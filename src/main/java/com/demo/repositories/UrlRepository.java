package com.demo.repositories;

import com.demo.entities.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID>, JpaSpecificationExecutor<Url> {
    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Url u
            SET u.viewCount = u.viewCount + :viewCount
            WHERE u.shortCode = :shortCode
            """)
    void incrementViewCount(String shortCode, Long viewCount);

    Optional<Url> findFirstByUser_IdAndBaseUrlHash(UUID userId, String baseUrlHash);

    @Query("""
            SELECT u.shortCode
            FROM Url u
            WHERE u.user IS NULL AND u.expiresAt < :now
            """)
    List<String> findExpiredGuestShortCodes(LocalDateTime now);

    @Query("""
            SELECT u.shortCode
            FROM Url u
            WHERE u.user IS NOT NULL AND u.expiresAt < :now AND u.status != com.demo.entities.enums.UrlStatus.EXPIRED
            """)
    List<String> findExpiredUserShortCodes(LocalDateTime now);

    Page<Url> findAll(Specification<Url> spec, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Url u WHERE u.user IS NULL AND u.expiresAt < :now")
    void deleteExpiredGuestUrls(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("Update Url u SET u.status = 'EXPIRED' WHERE u.user IS NOT NULL AND u.expiresAt < :now")
    void markExpiredUserUrls(LocalDateTime now);

    Optional<Url> findByIdAndUser_Id(UUID id, UUID userId);
}
