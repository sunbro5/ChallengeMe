package org.jan.achievement;

import org.jan.game.EventStatus;
import org.jan.game.GameEvent;
import org.jan.game.GameEventRepository;
import org.jan.notification.NotificationService;
import org.jan.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks and awards achievements after a game is resolved to FINISHED.
 * Called from {@link org.jan.game.GameEventService#resolve(GameEvent)}.
 */
@Service
public class AchievementService {

    @Autowired private AchievementRepository  achievementRepository;
    @Autowired private GameEventRepository    gameEventRepository;
    @Autowired private NotificationService    notificationService;

    /**
     * Evaluate all achievement conditions for the given user after a finished event.
     * Silently skips awards the user already has.
     */
    @Transactional
    public void evaluate(User user, GameEvent finishedEvent) {
        // Load all existing achievements in one query → avoids N+1 in tryAward()
        Set<AchievementType> already = achievementRepository
                .findByUserOrderByAwardedAtAsc(user)
                .stream()
                .map(UserAchievement::getAchievementType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AchievementType.class)));

        // Re-fetch all finished games (bulk-increment may not be visible in the same session)
        List<GameEvent> allFinished = gameEventRepository.findByParticipantAndStatusIn(
                user, List.of(EventStatus.FINISHED));

        int totalGames = allFinished.size();
        long userId    = user.getId();
        int totalWins  = (int) allFinished.stream()
                .filter(e -> e.getWinner() != null && e.getWinner().getId().equals(userId))
                .count();

        // ── Volume achievements ───────────────────────────────────────────────
        tryAward(user, AchievementType.GAMES_10, totalGames >= 10,  already);
        tryAward(user, AchievementType.GAMES_50, totalGames >= 50,  already);

        // ── Win achievements ──────────────────────────────────────────────────
        tryAward(user, AchievementType.FIRST_WIN, totalWins >= 1,   already);
        tryAward(user, AchievementType.WINS_5,    totalWins >= 5,   already);
        tryAward(user, AchievementType.WINS_10,   totalWins >= 10,  already);
        tryAward(user, AchievementType.WINS_25,   totalWins >= 25,  already);

        // ── Variety: won in at least 5 different game types ──────────────────
        long distinctWonTypes = allFinished.stream()
                .filter(e -> e.getWinner() != null && e.getWinner().getId().equals(userId))
                .map(e -> e.getGameType().name())
                .distinct()
                .count();
        tryAward(user, AchievementType.VARIETY_5, distinctWonTypes >= 5, already);

        // ── Win streak (sort oldest→newest, walk backwards) ──────────────────
        List<GameEvent> sorted = allFinished.stream()
                .sorted((a, b) -> a.getScheduledAt().compareTo(b.getScheduledAt()))
                .collect(Collectors.toList());

        int streak = 0;
        for (int i = sorted.size() - 1; i >= 0; i--) {
            GameEvent e = sorted.get(i);
            if (e.getWinner() != null && e.getWinner().getId().equals(userId)) {
                streak++;
            } else {
                break;
            }
        }

        tryAward(user, AchievementType.WIN_STREAK_3, streak >= 3, already);
        tryAward(user, AchievementType.WIN_STREAK_5, streak >= 5, already);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Awards the achievement if condition is true and the user doesn't have it already. */
    private void tryAward(User user, AchievementType type, boolean condition,
                          Set<AchievementType> already) {
        if (!condition || already.contains(type)) return;
        already.add(type); // prevent duplicate in same evaluate() call
        achievementRepository.save(new UserAchievement(user, type));
        notificationService.create(user, "ACHIEVEMENT_UNLOCKED", null,
                "You unlocked the achievement: " + type.name(), null);
    }
}
