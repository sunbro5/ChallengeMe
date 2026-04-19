package org.jan.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user_read", columnList = "user_id, read")
})
@Getter @Setter @NoArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String type;           // CHALLENGE_ACCEPTED, CHALLENGE_APPROVED, CHALLENGE_REJECTED, RESULT_SUBMITTED
    private String actorUsername;  // username of the user who triggered the notification
    private String text;
    private Long   eventId;
    private boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification(User user, String type, String actorUsername, String text, Long eventId) {
        this.user          = user;
        this.type          = type;
        this.actorUsername = actorUsername;
        this.text          = text;
        this.eventId       = eventId;
        this.createdAt     = LocalDateTime.now();
    }
}
