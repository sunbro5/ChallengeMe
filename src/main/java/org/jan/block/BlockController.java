package org.jan.block;

import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    @Autowired private BlockRepository blockRepository;
    @Autowired private UserRepository  userRepository;

    private User resolve(Authentication auth) {
        return userRepository.findByUsername(auth.getName());
    }

    @PostMapping("/{username}")
    public ResponseEntity<String> blockUser(@PathVariable String username, Authentication auth) {
        User blocker = resolve(auth);
        User blocked = userRepository.findByUsername(username);
        if (blocked == null) return ResponseEntity.badRequest().body("User not found");
        if (blocker.getId().equals(blocked.getId())) return ResponseEntity.badRequest().body("Cannot block yourself");
        if (blockRepository.existsByBlockerAndBlocked(blocker, blocked))
            return ResponseEntity.ok("Already blocked");
        blockRepository.save(new Block(blocker, blocked));
        return ResponseEntity.ok("User blocked");
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<String> unblockUser(@PathVariable String username, Authentication auth) {
        User blocker = resolve(auth);
        User blocked = userRepository.findByUsername(username);
        if (blocked == null) return ResponseEntity.badRequest().body("User not found");
        blockRepository.findByBlockerAndBlocked(blocker, blocked)
                .ifPresent(blockRepository::delete);
        return ResponseEntity.ok("User unblocked");
    }

    @GetMapping("/{username}")
    public ResponseEntity<Boolean> isBlocked(@PathVariable String username, Authentication auth) {
        User blocker = resolve(auth);
        User blocked = userRepository.findByUsername(username);
        if (blocked == null) return ResponseEntity.ok(false);
        return ResponseEntity.ok(blockRepository.existsByBlockerAndBlocked(blocker, blocked));
    }
}
