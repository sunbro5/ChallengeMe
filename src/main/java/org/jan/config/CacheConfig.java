package org.jan.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager();

        // Public map events — invalidated by @CacheEvict on state changes
        mgr.registerCustomCache("publicEvents",
            Caffeine.newBuilder().maximumSize(200).build());

        // Leaderboard — refreshes every 2 minutes (changes only after reportResult)
        mgr.registerCustomCache("leaderboard",
            Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(1).build());

        // Game catalogue — never changes at runtime, evicted only on restart
        mgr.registerCustomCache("games",
            Caffeine.newBuilder().maximumSize(1).build());

        return mgr;
    }
}
