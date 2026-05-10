package org.jan.teamtournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_tournaments")
@Getter @Setter @NoArgsConstructor
public class TeamTournament {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String name;

    private String gameType;

    /** Maximum number of teams allowed (2–32). */
    private int teamCapacity;

    /** Maximum players per team (2–20). */
    private int teamSizeMax;

    /** ELIMINATION or ROUND_ROBIN. */
    @Column(length = 20)
    private String format;

    /** GPS location of the tournament venue — saved when creator starts it. */
    @Column(columnDefinition = "double precision default 0")
    private double latitude;

    @Column(columnDefinition = "double precision default 0")
    private double longitude;

    /** OPEN, IN_PROGRESS, or FINISHED. */
    @Column(length = 20)
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private User creator;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_team_id", nullable = true)
    private TournamentTeam winnerTeam;
}
