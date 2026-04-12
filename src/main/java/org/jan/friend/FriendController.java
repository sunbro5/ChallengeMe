package org.jan.friend;

import org.jan.friend.dto.FriendDto;
import org.jan.friend.dto.FriendRequestDto;
import org.jan.friend.dto.UserSearchDto;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friends")
public class FriendController {

    @Autowired private FriendService  friendService;
    @Autowired private UserRepository userRepository;

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
                .findByUsernameContainingIgnoreCaseAndRoleNot(q.trim(), "ADMIN")
                .stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .limit(20)
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
