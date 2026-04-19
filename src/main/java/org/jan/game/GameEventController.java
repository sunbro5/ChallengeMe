package org.jan.game;

import org.jan.game.dto.ChallengeNotificationDto;
import org.jan.game.dto.GameEventDto;
import org.jan.notification.NotificationService;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class GameEventController {

    @Autowired private GameEventService      gameEventService;
    @Autowired private GameEventRepository   gameEventRepository;
    @Autowired private UserRepository        userRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private NotificationService   notificationService;

    // ── DTO builder ───────────────────────────────────────────────────────────

    private GameEventDto toDto(GameEvent event, User viewer) {
        boolean joined       = viewer != null && event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(viewer.getId()));
        boolean isCreator    = viewer != null && event.getCreator().getId().equals(viewer.getId());
        boolean isChallenger = joined && !isCreator;
        boolean iHaveSubmitted = (isCreator    && event.isCreatorResultSubmitted())
                              || (isChallenger && event.isChallengerResultSubmitted());

        User challenger = event.getChallenger();
        boolean disputed = event.getStatus() == EventStatus.DISPUTED;

        return GameEventDto.builder()
                .id(event.getId())
                .creatorUsername(event.getCreator().getUsername())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .gameType(event.getGameType().name())
                .scheduledAt(event.getScheduledAt())
                .status(event.getStatus().name())
                .participants(event.getParticipants().stream()
                        .map(User::getUsername).collect(Collectors.toList()))
                .joined(joined)
                .isCreator(isCreator)
                .isChallenger(isChallenger)
                .challengerUsername(challenger != null ? challenger.getUsername() : null)
                .creatorResultSubmitted(event.isCreatorResultSubmitted())
                .challengerResultSubmitted(event.isChallengerResultSubmitted())
                .iHaveSubmitted(iHaveSubmitted)
                .winnerUsername(event.getWinner() != null ? event.getWinner().getUsername() : null)
                .resultNote(event.getResultNote())
                .creatorResult(disputed ? resultLabel(event.getCreatorResult()) : null)
                .challengerResult(disputed ? resultLabel(event.getChallengerResult()) : null)
                .description(event.getDescription())
                .locationName(event.getLocationName())
                .invitedUsername(event.getInvitedUsername())
                .build();
    }

    private String resultLabel(String r) {
        return (r == null || r.isBlank()) ? "DRAW" : r;
    }

    private User resolve(Authentication auth) {
        return userRepository.findByUsername(auth.getName());
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @GetMapping("/api/events")
    public ResponseEntity<List<GameEventDto>> getActiveEvents(Authentication auth) {
        User viewer = resolve(auth);
        return ResponseEntity.ok(gameEventService.getActiveEvents().stream()
                .map(e -> toDto(e, viewer)).collect(Collectors.toList()));
    }

    @GetMapping("/api/events/mine")
    public ResponseEntity<List<GameEventDto>> getMyEvents(Authentication auth) {
        User user = resolve(auth);
        return ResponseEntity.ok(gameEventService.getMyEvents(user).stream()
                .map(e -> toDto(e, user)).collect(Collectors.toList()));
    }

    @GetMapping("/api/events/{id}")
    public ResponseEntity<GameEventDto> getEvent(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(toDto(gameEventService.getEvent(id), resolve(auth)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/events")
    public ResponseEntity<?> createEvent(@RequestBody GameEventCreateRequest req, Authentication auth) {
        try {
            User user = resolve(auth);
            GameEvent event = gameEventService.createEvent(
                    user, req.getLatitude(), req.getLongitude(),
                    req.getGameType(), req.getScheduledAt(), req.getDescription(),
                    req.getLocationName(), req.getInvitedUsername());
            // Notify the invited user (if any)
            if (req.getInvitedUsername() != null && !req.getInvitedUsername().isBlank()) {
                User invited = userRepository.findByUsername(req.getInvitedUsername());
                if (invited != null) {
                    notificationService.create(invited, "CHALLENGE_INVITE",
                            user.getUsername(), user.getUsername() + " invited you to a challenge!", event.getId());
                }
            }
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Challenger applies — moves to PENDING_APPROVAL and notifies creator. */
    @PostMapping("/api/events/{id}/accept")
    public ResponseEntity<?> acceptChallenge(@PathVariable Long id, Authentication auth) {
        try {
            User user  = resolve(auth);
            GameEvent event = gameEventService.acceptChallenge(user, id);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + event.getCreator().getUsername(),
                    new ChallengeNotificationDto(
                            "CHALLENGE_ACCEPTED",
                            event.getId(),
                            user.getUsername(),
                            event.getGameType().name()));
            notificationService.create(event.getCreator(), "CHALLENGE_ACCEPTED",
                    user.getUsername(), user.getUsername() + " accepted your challenge!", event.getId());
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/events/{id}/approve")
    public ResponseEntity<?> approveChallenger(@PathVariable Long id, Authentication auth) {
        try {
            User user = resolve(auth);
            GameEvent event = gameEventService.approveChallenger(user, id);
            if (event.getChallenger() != null) {
                notificationService.create(event.getChallenger(), "CHALLENGE_APPROVED",
                        user.getUsername(), user.getUsername() + " approved your challenge! Head to the location.", id);
            }
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/events/{id}/reject")
    public ResponseEntity<?> rejectChallenger(@PathVariable Long id, Authentication auth) {
        try {
            User user = resolve(auth);
            GameEvent event = gameEventService.rejectChallenger(user, id);
            if (event.getChallenger() != null) {
                notificationService.create(event.getChallenger(), "CHALLENGE_REJECTED",
                        user.getUsername(), user.getUsername() + " rejected your application.", id);
            }
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/api/events/{id}")
    public ResponseEntity<String> cancelEvent(@PathVariable Long id, Authentication auth) {
        try {
            gameEventService.cancelEvent(resolve(auth), id);
            return ResponseEntity.ok("Event cancelled");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @CacheEvict(value = "leaderboard", allEntries = true)
    @PostMapping("/api/events/{id}/result")
    public ResponseEntity<?> reportResult(@PathVariable Long id,
                                          @RequestBody GameResultRequest req,
                                          Authentication auth) {
        try {
            User user = resolve(auth);
            GameEvent event = gameEventService.reportResult(user, id, req.getWinnerUsername(), req.getResultNote());
            // Notify the other participant that a result was submitted
            event.getParticipants().stream()
                    .filter(p -> !p.getId().equals(user.getId()))
                    .findFirst()
                    .ifPresent(other -> notificationService.create(other, "RESULT_SUBMITTED",
                            user.getUsername(), user.getUsername() + " submitted their result.", id));
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Public map (no auth) ──────────────────────────────────────────────────

    @GetMapping("/api/events/public")
    public ResponseEntity<List<PublicEventDto>> getPublicEvents() {
        return ResponseEntity.ok(gameEventService.getPublicEvents());
    }

    // ── Leaderboard ───────────────────────────────────────────────────────────

    @Cacheable("leaderboard")
    @GetMapping("/api/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard() {
        return ResponseEntity.ok(userRepository.findLeaderboard().stream()
                .limit(50)
                .map(u -> new LeaderboardEntryDto(
                        u.getUsername(), u.getWins(), u.getLosses(), u.getDraws(), u.getDisputes(), u.getRating()))
                .collect(Collectors.toList()));
    }

    // ── Leaderboard by game type ──────────────────────────────────────────────

    @GetMapping("/api/leaderboard/game/{gameType}")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboardByGame(
            @PathVariable String gameType) {
        GameType type;
        try {
            type = GameType.valueOf(gameType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<GameEvent> events = gameEventRepository.findFinishedByGameType(type);

        Map<String, int[]> stats = new HashMap<>(); // {wins, losses, draws}
        for (GameEvent ev : events) {
            for (User p : ev.getParticipants()) {
                stats.putIfAbsent(p.getUsername(), new int[3]);
                int[] s = stats.get(p.getUsername());
                if (ev.getWinner() == null) {
                    s[2]++;
                } else if (ev.getWinner().getId().equals(p.getId())) {
                    s[0]++;
                } else {
                    s[1]++;
                }
            }
        }

        return ResponseEntity.ok(
                stats.entrySet().stream()
                        .map(e -> new LeaderboardEntryDto(e.getKey(),
                                e.getValue()[0], e.getValue()[1], e.getValue()[2], 0, 0))
                        .sorted(Comparator.comparingInt(LeaderboardEntryDto::wins).reversed())
                        .limit(50)
                        .collect(Collectors.toList()));
    }

    // ── Inner DTO for leaderboard (local — no need for a separate file) ───────
    public record LeaderboardEntryDto(String username, int wins, int losses, int draws, int disputes, int rating) {}
}
