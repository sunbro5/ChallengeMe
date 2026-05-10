package org.jan.game;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicEventCacheService {

    @Autowired private GameEventRepository gameEventRepository;
    @Autowired private CacheManager cacheManager;

    @SuppressWarnings("unchecked")
    private Cache<Object, Object> native_() {
        return (Cache<Object, Object>) cacheManager.getCache("publicEvents").getNativeCache();
    }

    /**
     * Returns all events currently in cache (up to 200).
     * On first call (empty cache): 1 DB query, populates cache.
     * Subsequent calls: 0 DB queries — values read directly from Caffeine map.
     */
    public List<PublicEventDto> getTop200Open() {
        Cache<Object, Object> cache = native_();
        if (cache.asMap().isEmpty()) {
            gameEventRepository
                    .findTop200ByStatusOrderByCreatedAtDesc(EventStatus.OPEN)
                    .stream()
                    // Only PUBLIC events are shown on the unauthenticated map
                    .filter(e -> e.getVisibility() == null || "PUBLIC".equals(e.getVisibility()))
                    .forEach(e -> cache.put(e.getId(), new PublicEventDto(
                            e.getId(), e.getLatitude(), e.getLongitude(), e.getGameType().name())));
        }
        return cache.asMap().values().stream()
                .map(v -> (PublicEventDto) v)
                .collect(Collectors.toList());
    }

    /**
     * Add or refresh a single event (called after createEvent or rejectChallenger).
     * If cache already holds 200 entries Caffeine automatically discards the oldest.
     */
    public void put(GameEvent event) {
        native_().put(event.getId(), new PublicEventDto(
                event.getId(), event.getLatitude(), event.getLongitude(),
                event.getGameType().name()));
    }

    /**
     * Remove a single event by ID (called when event leaves OPEN:
     * cancelEvent, acceptChallenge).
     */
    public void evict(Long id) {
        native_().invalidate(id);
    }
}
