package org.jan.tournament;

import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<TournamentDto>> getActive() {
        List<TournamentDto> list = tournamentService.getActive().stream()
                .map(t -> tournamentService.getDetail(t.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Get full detail + bracket for a specific tournament */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentDto> getDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new tournament.
     * Body: { "name": "...", "gameType": "TABLE_TENNIS", "capacity": 4 }
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User creator = userRepository.findByUsername(auth.getName());

        String name     = (String) body.get("name");
        String gameType = (String) body.get("gameType");
        int    capacity = Integer.parseInt(body.get("capacity").toString());

        try {
            Tournament t = tournamentService.createTournament(creator, name, gameType, capacity);
            return ResponseEntity.ok(tournamentService.getDetail(t.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Sign up to a tournament */
    @PostMapping("/{id}/join")
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
     * Body: { "latitude": 50.0, "longitude": 14.4 }
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id,
                                   @RequestBody Map<String, Object> body,
                                   Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User creator = userRepository.findByUsername(auth.getName());
        double lat = Double.parseDouble(body.get("latitude").toString());
        double lng = Double.parseDouble(body.get("longitude").toString());
        try {
            tournamentService.start(creator, id, lat, lng);
            return ResponseEntity.ok(tournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Record the winner of a match (creator only).
     * Body: { "winnerUserId": 42, "latitude": 50.0, "longitude": 14.4 }
     */
    @PostMapping("/{tournamentId}/matches/{matchId}/result")
    public ResponseEntity<?> recordResult(@PathVariable Long tournamentId,
                                          @PathVariable Long matchId,
                                          @RequestBody Map<String, Object> body,
                                          Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User requestor = userRepository.findByUsername(auth.getName());
        Long winnerUserId = Long.valueOf(body.get("winnerUserId").toString());
        double lat = Double.parseDouble(body.getOrDefault("latitude", "50.0755").toString());
        double lng = Double.parseDouble(body.getOrDefault("longitude", "14.4378").toString());
        try {
            tournamentService.recordResult(requestor, matchId, winnerUserId, lat, lng);
            return ResponseEntity.ok(tournamentService.getDetail(tournamentId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
