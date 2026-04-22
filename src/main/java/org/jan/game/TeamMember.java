package org.jan.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

import java.time.LocalDateTime;

/**
 * Extra players who join a PUB_QUIZ team after the event is IN_PROGRESS.
 * The creator and challenger are the "captains" — they live in game_event_participants.
 * Team members here are everyone else (side = "CREATOR" or "CHALLENGER").
 * ELO is only updated for the captains; team members just appear in the UI.
 */
@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private GameEvent event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** "CREATOR" — joins the host's team; "CHALLENGER" — joins the other team. */
    @Column(nullable = false, length = 12)
    private String side;

    private LocalDateTime joinedAt;

    public TeamMember(GameEvent event, User user, String side) {
        this.event    = event;
        this.user     = user;
        this.side     = side;
        this.joinedAt = LocalDateTime.now();
    }
}
