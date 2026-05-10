package com.demo.schedulers;

import com.demo.repositories.UrlRepository;
import com.demo.services.CacheService;
import com.demo.utils.AppConstants;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCounterScheduler {
    private final CacheService cacheService;
    private final UrlRepository urlRepository;

    //Flush every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void flushViewCounts() {
        log.info("Flushing view counts to DB...");
        flushAllViewCountsToDB();
    }

    //Flush on  shutdown
    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown detected, flushing view counts to DB...");
        flushAllViewCountsToDB();
    }

    private void flushAllViewCountsToDB(){
        Set<String> keys = cacheService.getUrlViewsKeys();
        if(keys.isEmpty()) return;

        for(String key : keys){
            String shortCode = key.replace(AppConstants.URL_VIEWS_KEY, "");
            //Fetch views for specific url, reset in cache and save in database.
            Long viewCount = cacheService.getAndResetViews(shortCode);
            if(viewCount != null && viewCount > 0){
                urlRepository.incrementViewCount(shortCode, viewCount);
            }

        }
    }
}
