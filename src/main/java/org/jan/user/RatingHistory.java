package org.jan.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One data point in a player's ELO history.
 * Written every time {@code GameEventService.applyElo()} runs.
 */
@Entity
@Table(name = "rating_history",
    indexes = @Index(name = "idx_rh_user_recorded", columnList = "user_id, recorded_at"))
@Getter @Setter @NoArgsConstructor
public class RatingHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rating_after", nullable = false)
    private int ratingAfter;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public RatingHistory(User user, int ratingAfter) {
        this.user        = user;
        this.ratingAfter = ratingAfter;
        this.recordedAt  = LocalDateTime.now();
    }
}
