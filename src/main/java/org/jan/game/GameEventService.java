package org.jan.game;

import org.jan.achievement.AchievementService;
import org.jan.config.ProfanityFilter;
import org.jan.user.RatingHistory;
import org.jan.user.RatingHistoryRepository;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameEventService {

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfanityFilter profanityFilter;

    @Autowired
    private PublicEventCacheService publicEventCacheService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private RatingHistoryRepository ratingHistoryRepository;

    static final int DAILY_EVENT_LIMIT = 5;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public GameEvent createEvent(User creator, double latitude, double longitude,
                                  GameType gameType, LocalDateTime scheduledAt,
                                  String description, String locationName, String invitedUsername,
                                  boolean teamMode) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayCount = gameEventRepository.countByCreatorSince(creator, startOfDay);
        if (todayCount >= DAILY_EVENT_LIMIT) {
            throw new IllegalArgumentException(
                    "You have reached the daily limit of " + DAILY_EVENT_LIMIT + " challenges. Try again tomorrow.");
        }

        GameEvent event = new GameEvent();
        event.setCreator(creator);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        event.setGameType(gameType);
        event.setScheduledAt(scheduledAt);
        if (description != null && !description.isBlank()) {
            profanityFilter.check(description);
        }
        event.setDescription(description != null ? description.strip() : null);
        event.setLocationName(locationName != null && !locationName.isBlank()
                ? locationName.strip() : null);
        event.setInvitedUsername(invitedUsername != null && !invitedUsername.isBlank()
                ? invitedUsername.strip() : null);
        event.setTeamMode(teamMode);
        event.setCreatedAt(LocalDateTime.now());
        event.setStatus(EventStatus.OPEN);
        event.getParticipants().add(creator);
        GameEvent saved = gameEventRepository.save(event);
        publicEventCacheService.put(saved);
        return saved;
    }

    // ── Accept challenge ──────────────────────────────────────────────────────

    /**
     * A challenger applies to join.  The event moves OPEN → PENDING_APPROVAL
     * so the creator can approve or reject.
     */
    @Transactional
    public GameEvent acceptChallenge(User challenger, Long eventId) {
        GameEvent event = load(eventId);

        if (event.getStatus() != EventStatus.OPEN)
            throw new IllegalArgumentException("This challenge is no longer open");

        if (isParticipant(event, challenger))
            throw new IllegalArgumentException("You already joined this event");

        // If creator sent a direct invite, only that user can accept
        String invited = event.getInvitedUsername();
        if (invited != null && !invited.equalsIgnoreCase(challenger.getUsername()))
            throw new IllegalArgumentException("This challenge is a private invite for another player");

        event.getParticipants().add(challenger);
        event.setStatus(EventStatus.PENDING_APPROVAL);
        GameEvent saved = gameEventRepository.save(event);
        publicEventCacheService.evict(saved.getId());
        return saved;
    }

    // ── Approve / reject ──────────────────────────────────────────────────────

    /**
     * Creator approves the challenger → IN_PROGRESS (players head to the location).
     */
    @Transactional
    public GameEvent approveChallenger(User creator, Long eventId) {
        GameEvent event = load(eventId);

        if (!event.getCreator().getId().equals(creator.getId()))
            throw new IllegalArgumentException("Only the creator can approve challengers");

        if (event.getStatus() != EventStatus.PENDING_APPROVAL)
            throw new IllegalArgumentException("No pending challenger to approve");

        event.setStatus(EventStatus.IN_PROGRESS);
        return gameEventRepository.save(event);
    }

    /**
     * Creator rejects the challenger → removes challenger, back to OPEN.
     */
    @Transactional
    public GameEvent rejectChallenger(User creator, Long eventId) {
        GameEvent event = load(eventId);

        if (!event.getCreator().getId().equals(creator.getId()))
            throw new IllegalArgumentException("Only the creator can reject challengers");

        if (event.getStatus() != EventStatus.PENDING_APPROVAL)
            throw new IllegalArgumentException("No pending challenger to reject");

        // Remove everyone except the creator
        event.getParticipants().removeIf(p -> !p.getId().equals(creator.getId()));
        event.setStatus(EventStatus.OPEN);
        GameEvent saved = gameEventRepository.save(event);
        publicEventCacheService.put(saved);
        return saved;
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Transactional
    public void cancelEvent(User user, Long eventId) {
        GameEvent event = load(eventId);

        if (event.getStatus() == EventStatus.FINISHED || event.getStatus() == EventStatus.DISPUTED)
            throw new IllegalArgumentException("Cannot cancel a completed event");

        if (!event.getCreator().getId().equals(user.getId()))
            throw new IllegalArgumentException("Only the creator can cancel the event");

        event.setStatus(EventStatus.CANCELLED);
        gameEventRepository.save(event);
        publicEventCacheService.evict(eventId);
    }

    // ── Report result (per-player) ────────────────────────────────────────────

    /**
     * Each participant submits their version of the result independently.
     * <ul>
     *   <li>If both submissions match → FINISHED, scores updated.</li>
     *   <li>If they disagree       → DISPUTED, disputes counter incremented for both.</li>
     * </ul>
     *
     * @param reporter       the user submitting
     * @param eventId        the event
     * @param winnerUsername winner's username, or null/blank for a draw
     * @param resultNote     optional note
     */
    @Transactional
    public GameEvent reportResult(User reporter, Long eventId,
                                   String winnerUsername, String resultNote) {
        GameEvent event = load(eventId);

        if (event.getStatus() != EventStatus.IN_PROGRESS)
            throw new IllegalArgumentException("Can only report a result for an in-progress event");

        // Result must be submitted within 24 hours of the scheduled time.
        // Prevents fraudulent backdated games created purely for ELO farming.
        if (LocalDateTime.now().isAfter(event.getScheduledAt().plusHours(24)))
            throw new IllegalArgumentException(
                    "Result submission window has expired (24 h after scheduled time). " +
                    "Contact an admin if you need assistance.");

        if (!isParticipant(event, reporter))
            throw new IllegalArgumentException("Only participants can report the result");

        boolean reporterIsCreator = event.getCreator().getId().equals(reporter.getId());

        // Normalise: blank/null → "" (means draw), else trim to username
        String normResult = (winnerUsername == null || winnerUsername.isBlank()) ? "" : winnerUsername.trim();

        // Validate winner is a participant (if not draw)
        if (!normResult.isEmpty()) {
            boolean validWinner = event.getParticipants().stream()
                    .anyMatch(p -> p.getUsername().equals(normResult));
            if (!validWinner)
                throw new IllegalArgumentException("Winner must be one of the event participants");
        }

        if (reporterIsCreator) {
            if (event.isCreatorResultSubmitted())
                throw new IllegalArgumentException("You already submitted your result");
            event.setCreatorResult(normResult);
            event.setCreatorResultNote(resultNote);
        } else {
            if (event.isChallengerResultSubmitted())
                throw new IllegalArgumentException("You already submitted your result");
            event.setChallengerResult(normResult);
            event.setChallengerResultNote(resultNote);
        }

        // Resolve only when BOTH have submitted
        if (event.isCreatorResultSubmitted() && event.isChallengerResultSubmitted()) {
            resolve(event);
        }

        return gameEventRepository.save(event);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PublicEventDto> getPublicEvents() {
        return publicEventCacheService.getTop200Open();
    }

    /** Events shown on the map: OPEN, PENDING_APPROVAL, IN_PROGRESS. */
    @Transactional(readOnly = true)
    public List<GameEvent> getActiveEvents() {
        return gameEventRepository.findByStatusIn(
                List.of(EventStatus.OPEN, EventStatus.PENDING_APPROVAL, EventStatus.IN_PROGRESS));
    }

    @Transactional(readOnly = true)
    public GameEvent getEvent(Long id) { return load(id); }

    /** All events where the user is a participant (creator or challenger), newest scheduled first. */
    @Transactional(readOnly = true)
    public List<GameEvent> getMyEvents(User user) {
        return gameEventRepository.findAllByParticipantOrderByScheduledAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<GameEvent> getPlayerHistory(User player) {
        return gameEventRepository.findByParticipantAndStatusIn(
                player, List.of(EventStatus.FINISHED, EventStatus.DISPUTED));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private GameEvent load(Long id) {
        // findByIdWithParticipants uses JOIN FETCH so participants are initialised
        // even after the transaction closes (e.g. when toDto() runs in the controller).
        return gameEventRepository.findByIdWithParticipants(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    private boolean isParticipant(GameEvent event, User user) {
        return event.getParticipants().stream().anyMatch(p -> p.getId().equals(user.getId()));
    }

    /**
     * Called when both players have submitted.  Compares results:
     *   agree  → FINISHED, update win/loss/draw counts
     *   disagree → DISPUTED, increment disputes for each participant
     */
    private void resolve(GameEvent event) {
        String cr  = event.getCreatorResult();    // "" = draw
        String chr = event.getChallengerResult(); // "" = draw

        if (cr.equals(chr)) {
            // ── Agreement ──────────────────────────────────────────────────
            event.setStatus(EventStatus.FINISHED);

            // Use creator's note first, fall back to challenger's note
            String note = (event.getCreatorResultNote() != null && !event.getCreatorResultNote().isBlank())
                    ? event.getCreatorResultNote()
                    : event.getChallengerResultNote();
            event.setResultNote(note);

            if (cr.isEmpty()) {
                // Draw — 1 bulk UPDATE
                event.setWinner(null);
                userRepository.bulkIncrementDraws(
                        event.getParticipants().stream().map(User::getId).toList());
            } else {
                // Win — 2 bulk UPDATEs
                User winner = event.getParticipants().stream()
                        .filter(p -> p.getUsername().equals(cr))
                        .findFirst()
                        .orElseThrow();
                event.setWinner(winner);
                userRepository.bulkIncrementWins(List.of(winner.getId()));
                List<Long> loserIds = event.getParticipants().stream()
                        .filter(p -> !p.getId().equals(winner.getId()))
                        .map(User::getId).toList();
                userRepository.bulkIncrementLosses(loserIds);
            }
        } else {
            // ── Disagreement — 1 bulk UPDATE ───────────────────────────────
            event.setStatus(EventStatus.DISPUTED);
            userRepository.bulkIncrementDisputes(
                    event.getParticipants().stream().map(User::getId).toList());
        }

        // ── ELO update (only on agreement) ────────────────────────────────
        if (event.getStatus() == EventStatus.FINISHED && event.getParticipants().size() == 2) {
            List<User> parts = event.getParticipants();
            User pA = parts.get(0);
            User pB = parts.get(1);

            // Diminishing returns for repeated opponents within 7 days.
            // The query runs before the current event is persisted as FINISHED,
            // so count = number of *prior* matches this week between the same pair.
            long priorMatches = gameEventRepository.countRecentFinishedBetween(
                    pA, pB, LocalDateTime.now().minusDays(7));
            double kMultiplier = switch ((int) Math.min(priorMatches, 3)) {
                case 0 -> 1.0;  // 1st match this week: full ELO
                case 1 -> 0.6;  // 2nd match: 60 %
                case 2 -> 0.3;  // 3rd match: 30 %
                default -> 0.0; // 4th+ match: no ELO change
            };

            double scoreA = event.getWinner() == null ? 0.5
                    : event.getWinner().getId().equals(pA.getId()) ? 1.0 : 0.0;
            applyElo(pA, pB, scoreA, kMultiplier);
        }

        // ── Achievement evaluation ────────────────────────────────────────
        if (event.getStatus() == EventStatus.FINISHED) {
            for (User participant : event.getParticipants()) {
                achievementService.evaluate(participant, event);
            }
        }
    }

    // ── ELO calculation (K=32, floor 100) ────────────────────────────────────
    /**
     * @param kMultiplier  fraction of the full K-factor to apply (0.0–1.0).
     *                     Pass 1.0 for a normal match, less for repeated opponents.
     */
    private void applyElo(User pA, User pB, double scoreA, double kMultiplier) {
        int rA = pA.getRating();
        int rB = pB.getRating();
        double expA = 1.0 / (1.0 + Math.pow(10, (rB - rA) / 400.0));
        double K = 32 * kMultiplier;
        pA.setRating(Math.max(100, rA + (int) Math.round(K * (scoreA - expA))));
        pB.setRating(Math.max(100, rB + (int) Math.round(K * ((1 - scoreA) - (1 - expA)))));
        userRepository.saveAll(List.of(pA, pB));
        // Record history only when ELO actually changes (kMultiplier > 0)
        if (kMultiplier > 0) {
            ratingHistoryRepository.saveAll(List.of(
                    new RatingHistory(pA, pA.getRating()),
                    new RatingHistory(pB, pB.getRating())));
        }
    }
}
