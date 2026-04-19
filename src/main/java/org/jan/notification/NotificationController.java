package org.jan.notification;

import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService  notificationService;
    @Autowired private UserRepository       userRepository;

    private User resolve(Authentication auth) {
        return userRepository.findByUsername(auth.getName());
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication auth) {
        User user = resolve(auth);
        List<NotificationDto> dtos = notificationService.getForUser(user).stream()
                .map(n -> new NotificationDto(n.getId(), n.getType(), n.getActorUsername(),
                        n.getText(), n.getEventId(), n.isRead(), n.getCreatedAt().toString()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication auth) {
        return ResponseEntity.ok(notificationService.countUnread(resolve(auth)));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication auth) {
        notificationService.markAllRead(resolve(auth));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication auth) {
        notificationService.markRead(id, resolve(auth));
        return ResponseEntity.ok().build();
    }

    public record NotificationDto(Long id, String type, String actorUsername, String text,
                                   Long eventId, boolean read, String createdAt) {}
}
