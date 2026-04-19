package org.jan.report;

import org.jan.game.GameEvent;
import org.jan.game.GameEventRepository;
import org.jan.game.EventStatus;
import org.jan.report.dto.ReportDto;
import org.jan.report.dto.DisputedEventDto;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
}
