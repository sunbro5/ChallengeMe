package org.jan.teamtournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

@Entity
@Table(name = "tournament_team_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"}))
@Getter @Setter @NoArgsConstructor
public class TournamentTeamMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    private TournamentTeam team;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public TournamentTeamMember(TournamentTeam team, User user) {
        this.team = team;
        this.user = user;
    }
}
