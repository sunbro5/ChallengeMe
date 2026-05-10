package org.jan.tournament;

import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired private TournamentService    tournamentService;
    @Autowired private TournamentRepository tournamentRepository;
    @Autowired private UserRepository       userRepository;

    /** List active (OPEN + IN_PROGRESS) tournaments */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<TournamentDto>> getActive() {
        List<TournamentDto> list = tournamentService.getActive().stream()
                .map(t -> tournamentService.getDetail(t.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Get full detail + bracket for a specific tournament */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<TournamentDto> getDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new tournament.
     * Body: { "name": "...", "gameType": "TABLE_TENNIS", "capacity": 4, "format": "ELIMINATION" }
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User creator = userRepository.findByUsername(auth.getName());

        String name     = (String) body.get("name");
        String gameType = (String) body.get("gameType");
        String format   = body.get("format") instanceof String s ? s : "ELIMINATION";

        Object capRaw = body.get("capacity");
        if (capRaw == null) return ResponseEntity.badRequest().body("capacity is required");
        int capacity;
        try { capacity = Integer.parseInt(capRaw.toString()); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body("capacity must be a number"); }

        try {
            Tournament t = tournamentService.createTournament(creator, name, gameType, capacity, format);
            return ResponseEntity.ok(tournamentService.getDetail(t.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Sign up to a tournament */
    @PostMapping("/{id}/join")
    @Transactional
    public ResponseEntity<?> join(@PathVariable Long id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName());
        try {
            tournamentService.join(user, id);
            return ResponseEntity.ok(tournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Start the tournament (creator only).
     * Body: { "latitude": 50.0755, "longitude": 14.4378 }
     * lat/lng are used for GameEvent placement in ELIMINATION; ignored in ROUND_ROBIN.
     */
    @PostMapping("/{id}/start")
    @Transactional
    public ResponseEntity<?> start(@PathVariable Long id,
                                   @RequestBody Map<String, Object> body,
                                   Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User creator = userRepository.findByUsername(auth.getName());
        double lat = parseDouble(body.getOrDefault("latitude",  "50.0755"));
        double lng = parseDouble(body.getOrDefault("longitude", "14.4378"));
        try {
            tournamentService.start(creator, id, lat, lng);
            return ResponseEntity.ok(tournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Record the winner of a match (creator only).
     * Body: { "winnerUserId": 42 }
     * lat/lng for next-round GameEvents are taken from the tournament's stored location.
     */
    @PostMapping("/{tournamentId}/matches/{matchId}/result")
    @Transactional
    public ResponseEntity<?> recordResult(@PathVariable Long tournamentId,
                                          @PathVariable Long matchId,
                                          @RequestBody Map<String, Object> body,
                                          Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User requestor = userRepository.findByUsername(auth.getName());

        Object winnerRaw = body.get("winnerUserId");
        if (winnerRaw == null) return ResponseEntity.badRequest().body("winnerUserId is required");
        Long winnerUserId;
        try { winnerUserId = Long.valueOf(winnerRaw.toString()); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body("winnerUserId must be a number"); }

        try {
            tournamentService.recordResult(requestor, matchId, winnerUserId);
            return ResponseEntity.ok(tournamentService.getDetail(tournamentId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static double parseDouble(Object val) {
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return 0.0; }
    }
}
