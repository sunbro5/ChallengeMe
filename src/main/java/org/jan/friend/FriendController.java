package org.jan.friend;

import jakarta.servlet.http.HttpSession;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friends")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    private User currentUser(HttpSession session) {
        User s = (User) session.getAttribute("user");
        return s == null ? null : userRepository.findById(s.getId()).orElse(null);
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestParam String username, HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).body("Not logged in");
        try {
            friendService.sendRequest(user, username);
            return ResponseEntity.ok("Request sent");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long friendshipId, HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).body("Not logged in");
        try {
            friendService.acceptRequest(user, friendshipId);
            return ResponseEntity.ok("Request accepted");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getFriends(HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(friendService.getFriends(user).stream()
                .map(f -> Map.<String, Object>of("id", f.getId(), "username", f.getUsername()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<Map<String, Object>>> getPendingRequests(HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(friendService.getPendingRequests(user).stream()
                .map(f -> Map.<String, Object>of(
                        "id", f.getId(),
                        "requesterId", f.getRequester().getId(),
                        "requesterUsername", f.getRequester().getUsername()))
                .collect(Collectors.toList()));
    }
}
