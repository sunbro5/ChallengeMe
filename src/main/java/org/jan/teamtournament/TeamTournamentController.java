package org.jan.teamtournament;

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
@RequestMapping("/api/team-tournaments")
public class TeamTournamentController {

    @Autowired private TeamTournamentService teamTournamentService;
    @Autowired private UserRepository        userRepository;

    /** List active (OPEN + IN_PROGRESS) team tournaments. */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<TeamTournamentDto>> getActive() {
        List<TeamTournamentDto> list = teamTournamentService.getActive().stream()
                .map(t -> teamTournamentService.getDetail(t.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Get full detail for a specific team tournament. */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(teamTournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new team tournament.
     * Body: { "name", "gameType", "teamCapacity", "teamSizeMax", "format" }
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User creator = userRepository.findByUsername(auth.getName());

        String name         = (String) body.get("name");
        String gameType     = (String) body.get("gameType");
        String format       = (String) body.get("format");
        int teamCapacity    = Integer.parseInt(body.get("teamCapacity").toString());
        int teamSizeMax     = Integer.parseInt(body.get("teamSizeMax").toString());

        try {
            TeamTournament t = teamTournamentService.createTournament(
                    creator, name, gameType, teamCapacity, teamSizeMax, format);
            return ResponseEntity.ok(teamTournamentService.getDetail(t.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Create a team within a tournament.
     * Body: { "name": "Team Alpha" }
     */
    @PostMapping("/{id}/teams")
    @Transactional
    public ResponseEntity<?> createTeam(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body,
                                         Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User captain = userRepository.findByUsername(auth.getName());
        String teamName = (String) body.get("name");

        try {
            teamTournamentService.createTeam(captain, id, teamName);
            return ResponseEntity.ok(teamTournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Join an existing team. */
    @PostMapping("/{id}/teams/{teamId}/join")
    @Transactional
    public ResponseEntity<?> joinTeam(@PathVariable Long id,
                                       @PathVariable Long teamId,
                                       Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName());

        try {
            teamTournamentService.joinTeam(user, teamId);
            return ResponseEntity.ok(teamTournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Leave a team (captain cannot leave). */
    @DeleteMapping("/{id}/teams/{teamId}/leave")
    @Transactional
    public ResponseEntity<?> leaveTeam(@PathVariable Long id,
                                        @PathVariable Long teamId,
                                        Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName());

        try {
            teamTournamentService.leaveTeam(user, teamId);
            return ResponseEntity.ok(teamTournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Kick a member from a team (captain only, tournament must be OPEN). */
    @DeleteMapping("/{id}/teams/{teamId}/members/{userId}")
    @Transactional
    public ResponseEntity<?> kickMember(@PathVariable Long id,
                                        @PathVariable Long teamId,
                                        @PathVariable Long userId,
                                        Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User captain = userRepository.findByUsername(auth.getName());
        try {
            teamTournamentService.kickMember(captain, teamId, userId);
            return ResponseEntity.ok(teamTournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Start the tournament (creator only).
     * Body: { "latitude": 50.0755, "longitude": 14.4378 }
     */
    @PostMapping("/{id}/start")
    @Transactional
    public ResponseEntity<?> start(@PathVariable Long id,
                                   @RequestBody Map<String, Object> body,
                                   Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User creator = userRepository.findByUsername(auth.getName());
        double lat = parseDouble(body.getOrDefault("latitude",  "0"));
        double lng = parseDouble(body.getOrDefault("longitude", "0"));
        try {
            teamTournamentService.start(creator, id, lat, lng);
            return ResponseEntity.ok(teamTournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private static double parseDouble(Object val) {
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return 0.0; }
    }

    /**
     * Record the result of a match (creator only).
     * Body: { "result": "A" | "B" | "DRAW", "note": "3:1" }
     */
    @PostMapping("/{id}/matches/{matchId}/result")
    @Transactional
    public ResponseEntity<?> recordResult(@PathVariable Long id,
                                           @PathVariable Long matchId,
                                           @RequestBody Map<String, Object> body,
                                           Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User requestor = userRepository.findByUsername(auth.getName());

        String result = (String) body.get("result");
        String note   = body.containsKey("note") ? (String) body.get("note") : null;

        try {
            teamTournamentService.recordResult(requestor, matchId, result, note);
            return ResponseEntity.ok(teamTournamentService.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
