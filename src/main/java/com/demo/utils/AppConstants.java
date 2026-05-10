package com.demo.utils;

import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Set;

@NoArgsConstructor
public final class AppConstants {
    //Counters
    public static final String COUNTER_KEY = "counters:";

    //Expiration
    public static final Duration GUEST_URL_EXPIRATION = Duration.ofDays(30); //30 days
    public static final Duration URL_CACHE_EXPIRATION = Duration.ofDays(1);


    //Urls
    public static final String URL_KEY = "urls:";
    public static final String URL_VIEWS_KEY = "views:";

    public static final Set<String> RESERVED_WORDS = Set.of(
            "api", "admin", "health", "login", "signup",
            "dashboard", "static", "assets", "qr"
    );

}
