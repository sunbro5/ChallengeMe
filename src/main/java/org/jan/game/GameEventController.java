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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class GameEventController {

    @Autowired private GameEventService      gameEventService;
    @Autowired private GameEventRepository   gameEventRepository;
    @Autowired private UserRepository        userRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private NotificationService   notificationService;
    @Autowired private TeamMemberRepository  teamMemberRepository;

    /**
     * Game types that support the team-join feature.
     * Naturally team-oriented: group knowledge games, party games, team sports, dice bluffing.
     */
    private static final java.util.Set<GameType> TEAM_GAME_TYPES = java.util.Set.of(
            GameType.PUB_QUIZ,
            GameType.BEER_PONG,
            GameType.FOOTBALL,
            GameType.BASKETBALL,
            GameType.UNO,
            GameType.KNIFFEL,
            GameType.JENGA,
            GameType.DOMINO,
            GameType.MEXICO_DICE,
            GameType.LIAR_DICE
    );

    // ── DTO builder ───────────────────────────────────────────────────────────

    /**
     * Single-event variant — fetches team members from DB if needed.
     * Use for single-event endpoints (getEvent, accept, approve …).
     */
    private GameEventDto toDto(GameEvent event, User viewer) {
        boolean isTeamEvent = TEAM_GAME_TYPES.contains(event.getGameType()) && event.isTeamMode();
        List<TeamMember> members = isTeamEvent
                ? teamMemberRepository.findByEvent(event)
                : List.of();
        return buildDto(event, viewer, members);
    }

    /**
     * List-endpoint variant — uses pre-fetched team members to avoid N+1.
     * Call {@link #batchTeamMembers} first, then pass the per-event slice here.
     */
    private GameEventDto toDto(GameEvent event, User viewer, List<TeamMember> preloadedMembers) {
        return buildDto(event, viewer, preloadedMembers);
    }

    /** Core DTO assembly — shared by both toDto() overloads. */
    private GameEventDto buildDto(GameEvent event, User viewer, List<TeamMember> members) {
        boolean joined       = viewer != null && event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(viewer.getId()));
        boolean isCreator    = viewer != null && event.getCreator().getId().equals(viewer.getId());
        boolean isChallenger = joined && !isCreator;
        boolean iHaveSubmitted = (isCreator    && event.isCreatorResultSubmitted())
                              || (isChallenger && event.isChallengerResultSubmitted());

        User challenger = event.getChallenger();
        boolean disputed = event.getStatus() == EventStatus.DISPUTED;

        // ── Team fields ───────────────────────────────────────────────────────
        boolean isTeamEvent = TEAM_GAME_TYPES.contains(event.getGameType()) && event.isTeamMode();
        List<String> creatorTeam    = null;
        List<String> challengerTeam = null;
        String myTeamSide = null;

        if (isTeamEvent) {
            creatorTeam = new ArrayList<>();
            creatorTeam.add(event.getCreator().getUsername());
            members.stream()
                    .filter(m -> "CREATOR".equals(m.getSide()))
                    .map(m -> m.getUser().getUsername())
                    .forEach(creatorTeam::add);

            if (challenger != null) {
                challengerTeam = new ArrayList<>();
                challengerTeam.add(challenger.getUsername());
                members.stream()
                        .filter(m -> "CHALLENGER".equals(m.getSide()))
                        .map(m -> m.getUser().getUsername())
                        .forEach(challengerTeam::add);
            }

            if (viewer != null) {
                if (isCreator) {
                    myTeamSide = "CREATOR";
                } else if (isChallenger) {
                    myTeamSide = "CHALLENGER";
                } else {
                    myTeamSide = members.stream()
                            .filter(m -> m.getUser().getId().equals(viewer.getId()))
                            .map(TeamMember::getSide)
                            .findFirst()
                            .orElse(null);
                }
            }
        }

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
                .isTeamEvent(isTeamEvent)
                .creatorTeam(creatorTeam)
                .challengerTeam(challengerTeam)
                .myTeamSide(myTeamSide)
                .build();
    }

    /**
     * Single DB call to load all team members for a list of events.
     * Returns a map of eventId → member list so each toDto() call gets its slice.
     */
    private Map<Long, List<TeamMember>> batchTeamMembers(List<GameEvent> events) {
        List<GameEvent> teamEvents = events.stream()
                .filter(e -> TEAM_GAME_TYPES.contains(e.getGameType()) && e.isTeamMode())
                .toList();
        if (teamEvents.isEmpty()) return Map.of();
        return teamMemberRepository.findByEventIn(teamEvents).stream()
                .collect(Collectors.groupingBy(m -> m.getEvent().getId()));
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
        List<GameEvent> events = gameEventService.getActiveEvents();
        Map<Long, List<TeamMember>> teamMap = batchTeamMembers(events);
        return ResponseEntity.ok(events.stream()
                .map(e -> toDto(e, viewer, teamMap.getOrDefault(e.getId(), List.of())))
                .collect(Collectors.toList()));
    }

    @GetMapping("/api/events/mine")
    public ResponseEntity<List<GameEventDto>> getMyEvents(Authentication auth) {
        User user = resolve(auth);
        List<GameEvent> events = gameEventService.getMyEvents(user);
        Map<Long, List<TeamMember>> teamMap = batchTeamMembers(events);
        return ResponseEntity.ok(events.stream()
                .map(e -> toDto(e, user, teamMap.getOrDefault(e.getId(), List.of())))
                .collect(Collectors.toList()));
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
                    req.getLocationName(), req.getInvitedUsername(), req.isTeamMode());
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

    // ── Team quiz — join / leave ──────────────────────────────────────────────

    /** Any logged-in user can join a team for an IN_PROGRESS PUB_QUIZ event. */
    @PostMapping("/api/events/{id}/team/join")
    public ResponseEntity<?> joinTeam(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      Authentication auth) {
        try {
            String side = body.get("side");
            if (!"CREATOR".equals(side) && !"CHALLENGER".equals(side))
                return ResponseEntity.badRequest().body("side must be CREATOR or CHALLENGER");

            User user  = resolve(auth);
            GameEvent event = gameEventService.getEvent(id);

            if (!TEAM_GAME_TYPES.contains(event.getGameType()) || !event.isTeamMode())
                return ResponseEntity.badRequest().body("This event was not created as a team event");

            if (event.getStatus() != EventStatus.IN_PROGRESS)
                return ResponseEntity.badRequest().body("Can only join a team while the event is in progress");

            // Captains are already on their team implicitly
            if (event.getCreator().getId().equals(user.getId()))
                return ResponseEntity.badRequest().body("You are the creator — you are already captain of Team A");

            User challenger = event.getChallenger();
            if (challenger != null && challenger.getId().equals(user.getId()))
                return ResponseEntity.badRequest().body("You are the challenger — you are already captain of Team B");

            if ("CHALLENGER".equals(side) && challenger == null)
                return ResponseEntity.badRequest().body("No challenger yet — Team B does not exist");

            if (teamMemberRepository.existsByEventAndUser(event, user))
                return ResponseEntity.badRequest().body("You are already on a team for this event");

            // Max 7 extra members per side (8 total including captain)
            if (teamMemberRepository.countByEventAndSide(event, side) >= 7)
                return ResponseEntity.badRequest().body("That team is full (max 8 players per side)");

            teamMemberRepository.save(new TeamMember(event, user, side));
            return ResponseEntity.ok(toDto(event, user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Non-captain team members can leave before the event finishes. */
    @DeleteMapping("/api/events/{id}/team/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable Long id, Authentication auth) {
        try {
            User user  = resolve(auth);
            GameEvent event = gameEventService.getEvent(id);

            Optional<TeamMember> membership = teamMemberRepository.findByEventAndUser(event, user);
            if (membership.isEmpty())
                return ResponseEntity.badRequest().body("You are not on a team for this event");

            teamMemberRepository.delete(membership.get());
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
