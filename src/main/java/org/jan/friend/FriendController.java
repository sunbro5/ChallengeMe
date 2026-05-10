package org.jan.friend;

import org.jan.friend.dto.ActivityDto;
import org.jan.friend.dto.FriendDto;
import org.jan.friend.dto.FriendRequestDto;
import org.jan.friend.dto.UserSearchDto;
import org.jan.game.GameEvent;
import org.jan.game.GameEventRepository;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired private FriendService       friendService;
    @Autowired private UserRepository      userRepository;
    @Autowired private GameEventRepository gameEventRepository;

    private User resolveUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDto>> searchUsers(
            @RequestParam String q, Authentication auth) {
        User user = resolveUser(auth);
        if (q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<UserSearchDto> results = userRepository
                .findTop20ByUsernameContainingIgnoreCaseAndRoleNot(q.trim(), "ADMIN")
                .stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .map(u -> new UserSearchDto(u.getId(), u.getUsername(),
                        friendService.getFriendshipStatus(user, u)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestParam String username, Authentication auth) {
        try {
            friendService.sendRequest(resolveUser(auth), username);
            return ResponseEntity.ok("Request sent");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long friendshipId, Authentication auth) {
        try {
            friendService.acceptRequest(resolveUser(auth), friendshipId);
            return ResponseEntity.ok("Request accepted");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/requests/{friendshipId}")
    public ResponseEntity<String> declineRequest(@PathVariable Long friendshipId, Authentication auth) {
        try {
            friendService.declineRequest(resolveUser(auth), friendshipId);
            return ResponseEntity.ok("Request declined");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/with/{friendId}")
    public ResponseEntity<String> unfriend(@PathVariable Long friendId, Authentication auth) {
        User friend = userRepository.findById(friendId).orElse(null);
        if (friend == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        friendService.unfriend(resolveUser(auth), friend);
        return ResponseEntity.ok("Unfriended");
    }

    @GetMapping
    public ResponseEntity<List<FriendDto>> getFriends(Authentication auth) {
        List<FriendDto> friends = friendService.getFriends(resolveUser(auth)).stream()
                .map(f -> new FriendDto(f.getId(), f.getUsername()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(friends);
    }

    /** Last 20 FINISHED games involving any of the current user's friends. */
    @Transactional(readOnly = true)
    @GetMapping("/activity")
    public ResponseEntity<List<ActivityDto>> getActivity(Authentication auth) {
        User user = resolveUser(auth);
        List<User> friends = friendService.getFriends(user);
        if (friends.isEmpty()) return ResponseEntity.ok(List.of());

        List<GameEvent> events = gameEventRepository.findFinishedByParticipantsIn(
                friends, PageRequest.of(0, 20));

        // Pre-build a Set for O(1) friend lookup instead of O(n) per event
        Set<String> friendNames = friends.stream()
                .map(User::getUsername)
                .collect(java.util.stream.Collectors.toSet());

        List<ActivityDto> feed = events.stream().map(e -> {
            String winnerUsername  = e.getWinner() != null ? e.getWinner().getUsername() : null;
            List<String> names = e.getParticipants().stream()
                    .map(User::getUsername).collect(Collectors.toList());
            // Find the friend who participated (for the "subject" line)
            String friend = names.stream()
                    .filter(friendNames::contains)
                    .findFirst().orElse(names.isEmpty() ? "" : names.get(0));
            String opponent = names.stream()
                    .filter(n -> !n.equals(friend))
                    .findFirst().orElse("");
            String outcome; // from the friend's perspective
            if (winnerUsername == null) {
                outcome = "draw";
            } else if (winnerUsername.equals(friend)) {
                outcome = "won";
            } else {
                outcome = "lost";
            }
            return new ActivityDto(
                    e.getId(),
                    friend,
                    opponent,
                    outcome,
                    e.getGameType().name(),
                    e.getScheduledAt()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(feed);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests(Authentication auth) {
        List<FriendRequestDto> requests = friendService.getPendingRequests(resolveUser(auth)).stream()
                .map(f -> new FriendRequestDto(
                        f.getId(),
                        f.getRequester().getId(),
                        f.getRequester().getUsername()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }
}
