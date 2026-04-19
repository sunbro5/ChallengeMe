package org.jan.game;

import org.jan.notification.NotificationService;
import org.jan.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically cancels/disputes events that are past their scheduled time
 * and were never completed.
 *
 * <ul>
 *   <li>OPEN events older than 24 h → CANCELLED</li>
 *   <li>IN_PROGRESS events older than 48 h → DISPUTED (both players notified)</li>
 * </ul>
 */
@Component
public class EventExpiryScheduler {

    @Autowired private GameEventRepository gameEventRepository;
    @Autowired private NotificationService  notificationService;

    /** Runs every hour. */
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void expireStaleEvents() {
        LocalDateTime now = LocalDateTime.now();

        // ── OPEN events: cancel after 24 h past scheduledAt ──────────────────
        List<GameEvent> staleOpen = gameEventRepository.findStaleOpen(now.minusHours(24));
        for (GameEvent ev : staleOpen) {
            ev.setStatus(EventStatus.CANCELLED);
            gameEventRepository.save(ev);
        }

        // ── IN_PROGRESS events: dispute after 48 h past scheduledAt ──────────
        List<GameEvent> staleInProgress = gameEventRepository.findStaleInProgress(now.minusHours(48));
        for (GameEvent ev : staleInProgress) {
            ev.setStatus(EventStatus.DISPUTED);
            gameEventRepository.save(ev);
            // Notify both players
            for (User p : ev.getParticipants()) {
                notificationService.create(p, "EVENT_EXPIRED", null,
                        "A game you were in expired without a result and was marked disputed.", ev.getId());
            }
        }
    }
}
