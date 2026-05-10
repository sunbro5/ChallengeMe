package org.jan.teamtournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

@Entity
@Table(name = "tournament_teams",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "name"}))
@Getter @Setter @NoArgsConstructor
public class TournamentTeam {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament_id")
    private TeamTournament tournament;

    @Column(length = 60)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "captain_id")
    private User captain;

    private int seed = 0;
    private boolean eliminated = false;
    private int points = 0;
    private int wins   = 0;
    private int draws  = 0;
    private int losses = 0;

    public TournamentTeam(TeamTournament tournament, String name, User captain) {
        this.tournament = tournament;
        this.name = name;
        this.captain = captain;
    }
}
