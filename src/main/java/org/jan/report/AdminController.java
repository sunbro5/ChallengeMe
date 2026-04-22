package org.jan.report;

import org.jan.game.GameEvent;
import org.jan.game.GameEventRepository;
import org.jan.game.EventStatus;
import org.jan.report.dto.DisputedEventDto;
import org.jan.report.dto.FraudPanelDto;
import org.jan.report.dto.ReportDto;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * All /admin/** endpoints are restricted to ROLE_ADMIN by SecurityConfig.
 * No manual role check needed here.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private ReportService      reportService;
    @Autowired private GameEventRepository gameEventRepository;
    @Autowired private UserRepository      userRepository;

    @GetMapping("/reports")
    public ResponseEntity<List<ReportDto>> getReports() {
        List<ReportDto> dtos = reportService.getAllReports().stream()
                .map(r -> new ReportDto(
                        r.getId(),
                        r.getReporter().getUsername(),
                        r.getReported().getUsername(),
                        r.getReported().getId(),
                        r.getReported().isBanned(),
                        r.getReason(),
                        r.getCreatedAt().toString(),
                        r.getStatus().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/ban/{userId}")
    public ResponseEntity<String> banUser(@PathVariable Long userId) {
        try { reportService.banUser(userId); return ResponseEntity.ok("User banned"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/unban/{userId}")
    public ResponseEntity<String> unbanUser(@PathVariable Long userId) {
        try { reportService.unbanUser(userId); return ResponseEntity.ok("User unbanned"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/resolve/{reportId}")
    public ResponseEntity<String> resolveReport(@PathVariable Long reportId) {
        try { reportService.resolveReport(reportId); return ResponseEntity.ok("Report resolved"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/disputes")
    public ResponseEntity<List<DisputedEventDto>> getDisputedEvents() {
        List<DisputedEventDto> dtos = gameEventRepository
                .findByStatus(EventStatus.DISPUTED)
                .stream()
                .map(e -> new DisputedEventDto(
                        e.getId(),
                        e.getGameType().name(),
                        e.getCreator().getUsername(),
                        e.getChallenger() != null ? e.getChallenger().getUsername() : null,
                        e.getCreatorResult(),
                        e.getChallengerResult(),
                        e.getScheduledAt() != null ? e.getScheduledAt().toString() : null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Transactional
    @PostMapping("/disputes/{eventId}/resolve")
    public ResponseEntity<String> resolveDispute(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> body) {
        GameEvent event = gameEventRepository.findById(eventId).orElse(null);
        if (event == null || event.getStatus() != EventStatus.DISPUTED)
            return ResponseEntity.badRequest().body("Disputed event not found");

        String winnerUsername = body.get("winnerUsername");
        if (winnerUsername == null || winnerUsername.isBlank()) {
            // Admin declares draw
            event.setWinner(null);
            userRepository.bulkIncrementDraws(
                    event.getParticipants().stream().map(u -> u.getId()).collect(Collectors.toList()));
        } else {
            var winner = event.getParticipants().stream()
                    .filter(u -> u.getUsername().equals(winnerUsername))
                    .findFirst().orElse(null);
            if (winner == null) return ResponseEntity.badRequest().body("Winner not found in participants");
            event.setWinner(winner);
            userRepository.bulkIncrementWins(List.of(winner.getId()));
            var loserIds = event.getParticipants().stream()
                    .filter(u -> !u.getId().equals(winner.getId()))
                    .map(u -> u.getId()).collect(Collectors.toList());
            userRepository.bulkIncrementLosses(loserIds);
        }
        event.setStatus(EventStatus.FINISHED);
        gameEventRepository.save(event);
        return ResponseEntity.ok("Dispute resolved");
    }

    // ── Fraud panel ───────────────────────────────────────────────────────────

    /**
     * Returns two fraud signals:
     *  1. Pairs that played each other ≥3 times in the last 7 days (collusion risk).
     *  2. Users with ≥5 total games whose dispute rate exceeds 25 %.
     */
    @Transactional(readOnly = true)
    @GetMapping("/fraud")
    public ResponseEntity<FraudPanelDto> getFraudPanel() {
        // ── Suspicious pairs ──────────────────────────────────────────────────
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        // Capped at 500 to avoid loading an unbounded result set into memory.
        List<GameEvent> recentFinished = gameEventRepository.findFinishedSince(since, PageRequest.of(0, 500));

        // Group by sorted pair key ("alice vs bob"), count occurrences
        Map<String, long[]> pairCounts = new LinkedHashMap<>();
        for (GameEvent e : recentFinished) {
            if (e.getParticipants().size() != 2) continue;
            List<String> names = e.getParticipants().stream()
                    .map(User::getUsername).sorted().toList();
            String key = names.get(0) + "|||" + names.get(1);
            pairCounts.computeIfAbsent(key, k -> new long[]{0})[0]++;
        }

        List<FraudPanelDto.SuspiciousPairDto> suspiciousPairs = pairCounts.entrySet().stream()
                .filter(entry -> entry.getValue()[0] >= 3)
                .sorted(Comparator.comparingLong((Map.Entry<String, long[]> e) -> e.getValue()[0]).reversed())
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|\\|\\|");
                    return new FraudPanelDto.SuspiciousPairDto(parts[0], parts[1], entry.getValue()[0]);
                })
                .toList();

        // ── High dispute-rate users ────────────────────────────────────────────
        // DB-side query already filters: role=USER, not banned, ≥5 games, dispute rate >25 %.
        List<FraudPanelDto.HighDisputeUserDto> highDisputeUsers = userRepository.findHighDisputeUsers().stream()
                .map(u -> {
                    int total = u.getWins() + u.getLosses() + u.getDraws();
                    double rate = (double) u.getDisputes() / total;
                    return new FraudPanelDto.HighDisputeUserDto(
                            u.getId(), u.getUsername(), u.isBanned(),
                            u.getWins(), u.getLosses(), u.getDraws(),
                            u.getDisputes(), total, rate);
                })
                .sorted(Comparator.comparingDouble(FraudPanelDto.HighDisputeUserDto::disputeRate).reversed())
                .toList();

        return ResponseEntity.ok(new FraudPanelDto(suspiciousPairs, highDisputeUsers));
    }

    /** Reset a user's ELO rating back to the starting value (1 000). */
    @Transactional
    @PostMapping("/users/{userId}/reset-elo")
    public ResponseEntity<String> resetElo(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        user.setRating(1000);
        userRepository.save(user);
        return ResponseEntity.ok("ELO reset to 1000");
    }
}
