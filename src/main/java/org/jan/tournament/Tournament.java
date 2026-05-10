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

    @Column(nullable = false)
    private int capacity;

    /** GPS location of the tournament venue — saved when creator starts it. */
    @Column(columnDefinition = "double precision default 0")
    private double latitude;

    @Column(columnDefinition = "double precision default 0")
    private double longitude;

    /**
     * ELIMINATION — single-elimination bracket (capacity must be 2, 4, 8 or 16)
     * ROUND_ROBIN — every participant plays every other participant
     */
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ELIMINATION' not null")
    private String formatType = "ELIMINATION";

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
