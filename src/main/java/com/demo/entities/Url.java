package com.demo.entities;

import com.demo.entities.enums.UrlStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "urls", indexes = {
        @Index(columnList = "short_code"),    // for redirect lookup
        @Index(columnList = "user_id"),       // for guest cleanup scheduler
        @Index(columnList = "user_id, base_url_hash")  // for duplicate detection
})
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(nullable = false)
    private String baseUrl;

    private String baseUrlHash;

    @JoinColumn(name = "user_id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long viewCount = 0L;

    private Instant expiresAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UrlStatus status = UrlStatus.ACTIVE;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedDate //we already applied Auditing
    private Instant createdAt;

}

