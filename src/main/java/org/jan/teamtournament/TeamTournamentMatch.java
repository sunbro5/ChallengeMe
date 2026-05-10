package org.jan.teamtournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_tournament_matches")
@Getter @Setter @NoArgsConstructor
public class TeamTournamentMatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament_id")
    private TeamTournament tournament;

    private int round;
    private int matchIndex;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_a_id")
    private TournamentTeam teamA;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_b_id")
    private TournamentTeam teamB;

    /** Null until result is recorded. Null AND status=FINISHED means draw. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_team_id", nullable = true)
    private TournamentTeam winnerTeam;

    /** PENDING or FINISHED. */
    @Column(length = 20)
    private String status = "PENDING";

    /** Optional note, e.g. final score "3:1". */
    @Column(length = 100)
    private String note;

    public TeamTournamentMatch(TeamTournament tournament, int round, int matchIndex,
                                TournamentTeam teamA, TournamentTeam teamB) {
        this.tournament = tournament;
        this.round = round;
        this.matchIndex = matchIndex;
        this.teamA = teamA;
        this.teamB = teamB;
    }
}
