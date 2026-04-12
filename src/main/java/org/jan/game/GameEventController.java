package org.jan.game;

import org.jan.game.dto.ChallengeNotificationDto;
import org.jan.game.dto.GameEventDto;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GameEventController {

    @Autowired private GameEventService      gameEventService;
    @Autowired private UserRepository        userRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

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
                .build();
    }

    private String resultLabel(String r) {
        return (r == null || r.isBlank()) ? "DRAW" : r;
    }

    private User resolve(Authentication auth) {
        return userRepository.findByUsername(auth.getName());
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @GetMapping("/events")
    public ResponseEntity<List<GameEventDto>> getActiveEvents(Authentication auth) {
        User viewer = resolve(auth);
        return ResponseEntity.ok(gameEventService.getActiveEvents().stream()
                .map(e -> toDto(e, viewer)).collect(Collectors.toList()));
    }

    @GetMapping("/events/mine")
    public ResponseEntity<List<GameEventDto>> getMyEvents(Authentication auth) {
        User user = resolve(auth);
        return ResponseEntity.ok(gameEventService.getMyEvents(user).stream()
                .map(e -> toDto(e, user)).collect(Collectors.toList()));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<GameEventDto> getEvent(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(toDto(gameEventService.getEvent(id), resolve(auth)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody GameEventCreateRequest req, Authentication auth) {
        try {
            User user = resolve(auth);
            GameEvent event = gameEventService.createEvent(
                    user, req.getLatitude(), req.getLongitude(),
                    req.getGameType(), req.getScheduledAt(), req.getDescription());
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Challenger applies — moves to PENDING_APPROVAL and notifies creator. */
    @PostMapping("/events/{id}/accept")
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
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/events/{id}/approve")
    public ResponseEntity<?> approveChallenger(@PathVariable Long id, Authentication auth) {
        try {
            User user = resolve(auth);
            return ResponseEntity.ok(toDto(gameEventService.approveChallenger(user, id), user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/events/{id}/reject")
    public ResponseEntity<?> rejectChallenger(@PathVariable Long id, Authentication auth) {
        try {
            User user = resolve(auth);
            return ResponseEntity.ok(toDto(gameEventService.rejectChallenger(user, id), user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<String> cancelEvent(@PathVariable Long id, Authentication auth) {
        try {
            gameEventService.cancelEvent(resolve(auth), id);
            return ResponseEntity.ok("Event cancelled");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/events/{id}/result")
    public ResponseEntity<?> reportResult(@PathVariable Long id,
                                          @RequestBody GameResultRequest req,
                                          Authentication auth) {
        try {
            User user = resolve(auth);
            return ResponseEntity.ok(toDto(
                    gameEventService.reportResult(user, id, req.getWinnerUsername(), req.getResultNote()),
                    user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Public map (no auth) ──────────────────────────────────────────────────

    @GetMapping("/events/public")
    public ResponseEntity<List<PublicEventDto>> getPublicEvents() {
        return ResponseEntity.ok(gameEventService.getPublicEvents());
    }

    // ── Leaderboard ───────────────────────────────────────────────────────────

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard() {
        return ResponseEntity.ok(userRepository.findLeaderboard().stream()
                .limit(50)
                .map(u -> new LeaderboardEntryDto(
                        u.getUsername(), u.getWins(), u.getLosses(), u.getDraws(), u.getDisputes()))
                .collect(Collectors.toList()));
    }

    // ── Inner DTO for leaderboard (local — no need for a separate file) ───────
    public record LeaderboardEntryDto(String username, int wins, int losses, int draws, int disputes) {}
}
