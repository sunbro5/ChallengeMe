package org.jan.sportsmanship;

import org.jan.game.EventStatus;
import org.jan.game.GameEvent;
import org.jan.game.GameEventRepository;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sportsmanship")
public class SportsmanshipController {

    @Autowired private SportsmanshipRepository sportsmanshipRepository;
    @Autowired private GameEventRepository     gameEventRepository;
    @Autowired private UserRepository          userRepository;

    /**
     * Submit a sportsmanship vote for the opponent in a finished game.
     * Body: { "eventId": 123, "positive": true }
     * Rules:
     *  - Event must be FINISHED
     *  - Voter must be a participant
     *  - Cannot vote for themselves
     *  - One vote per (voter, event) — enforced by unique constraint
     */
    @Transactional
    @PostMapping
    public ResponseEntity<?> vote(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        Object rawEventId = body.get("eventId");
        Object rawPositive = body.get("positive");
        if (rawEventId == null || rawPositive == null)
            return ResponseEntity.badRequest().body("eventId and positive are required");

        Long eventId  = Long.valueOf(rawEventId.toString());
        boolean positive = Boolean.parseBoolean(rawPositive.toString());

        User voter = userRepository.findByUsername(auth.getName());
        // Use JOIN FETCH to avoid N+1 when accessing event.getParticipants() below.
        GameEvent event = gameEventRepository.findByIdWithParticipants(eventId).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();

        if (event.getStatus() != EventStatus.FINISHED) {
            return ResponseEntity.badRequest().body("Can only vote for a finished game");
        }

        boolean isParticipant = event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(voter.getId()));
        if (!isParticipant) {
            return ResponseEntity.status(403).body("You are not a participant of this event");
        }

        // Determine the opponent (target = the other participant)
        User target = event.getParticipants().stream()
                .filter(p -> !p.getId().equals(voter.getId()))
                .findFirst().orElse(null);
        if (target == null) {
            return ResponseEntity.badRequest().body("No opponent found");
        }

        if (sportsmanshipRepository.existsByVoterAndEvent(voter, event)) {
            return ResponseEntity.badRequest().body("You already voted for this game");
        }

        sportsmanshipRepository.save(new Sportsmanship(voter, target, event, positive));
        return ResponseEntity.ok().build();
    }

    /**
     * Get sportsmanship counts for a user.
     * Returns: { "thumbsUp": N, "thumbsDown": N }
     */
    @Transactional(readOnly = true)
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Long>> getStats(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.notFound().build();

        long up   = sportsmanshipRepository.countByTargetAndPositive(user, true);
        long down = sportsmanshipRepository.countByTargetAndPositive(user, false);
        return ResponseEntity.ok(Map.of("thumbsUp", up, "thumbsDown", down));
    }

    /**
     * Check if the current user has already voted for a given event.
     * Returns: { "voted": true/false }
     */
    @Transactional(readOnly = true)
    @GetMapping("/check/{eventId}")
    public ResponseEntity<Map<String, Boolean>> checkVoted(
            @PathVariable Long eventId, Authentication auth) {
        if (auth == null) return ResponseEntity.ok(Map.of("voted", false));

        User voter = userRepository.findByUsername(auth.getName());
        // findByIdWithParticipants is cheaper when participants may be accessed elsewhere;
        // for existsByVoterAndEvent we only need the event identity, so findById is fine.
        GameEvent event = gameEventRepository.findById(eventId).orElse(null);
        if (event == null) return ResponseEntity.ok(Map.of("voted", false));

        boolean voted = sportsmanshipRepository.existsByVoterAndEvent(voter, event);
        return ResponseEntity.ok(Map.of("voted", voted));
    }
}
