package org.jan.tournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.jan.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    /** Key of the game type (e.g. "TABLE_TENNIS") */
    @Column(nullable = false)
    private String gameType;

    /** 4 or 8 */
    @Column(nullable = false)
    private int capacity;

    /**
     * OPEN         — accepting sign-ups
     * IN_PROGRESS  — bracket started
     * FINISHED     — winner determined
     */
    @Column(nullable = false)
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
