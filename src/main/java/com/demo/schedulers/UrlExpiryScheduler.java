package com.demo.schedulers;

import com.demo.repositories.UrlRepository;
import com.demo.services.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlExpiryScheduler {
    private final UrlRepository urlRepository;
    private final CacheService cacheService;

    //Runs every day at midnight
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void markExpiredUrls() {
        log.info("Starting mark expired URLs...");

        List<String> expiredShortCodes = urlRepository.findExpiredUserShortCodes(LocalDateTime.now());

        if (expiredShortCodes.isEmpty()) return;

        //Mark them as expired
        urlRepository.markExpiredUserUrls(LocalDateTime.now());

        //Evict from cache, only reached if DB succeeded
        expiredShortCodes.forEach(shortCode -> {
            cacheService.evictUrl(shortCode);
            cacheService.evictViews(shortCode);
        });

        log.info("Marked up {} expired user URLs.", expiredShortCodes.size());
    }
}