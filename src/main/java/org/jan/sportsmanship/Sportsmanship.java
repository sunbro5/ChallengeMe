package org.jan.sportsmanship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.game.GameEvent;
import org.jan.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "sportsmanship",
        uniqueConstraints = @UniqueConstraint(columnNames = {"voter_id", "event_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Sportsmanship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private GameEvent event;

    /** true = thumbs up, false = thumbs down */
    private boolean positive;

    private LocalDateTime votedAt;

    public Sportsmanship(User voter, User target, GameEvent event, boolean positive) {
        this.voter    = voter;
        this.target   = target;
        this.event    = event;
        this.positive = positive;
        this.votedAt  = LocalDateTime.now();
    }
}
