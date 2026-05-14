package com.demo.schedulers;

import com.demo.repositories.UrlRepository;
import com.demo.services.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestUrlCleanupScheduler {
    private final UrlRepository urlRepository;
    private final CacheService cacheService;

    //Runs every day at midnight
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredGuestUrls() {
        log.info("Starting guest URL cleanup...");

        List<String> expiredShortCodes = urlRepository.findExpiredGuestShortCodes(Instant.now());

        if (expiredShortCodes.isEmpty()) return;

        //Delete from DB
        urlRepository.deleteExpiredGuestUrls(Instant.now());

        //Evict from cache, only reached if DB delete succeeded
        expiredShortCodes.forEach(shortCode -> {
            cacheService.evictUrl(shortCode);
            cacheService.evictViews(shortCode);
        });

        log.info("Cleaned up {} expired guest URLs.", expiredShortCodes.size());
    }
}