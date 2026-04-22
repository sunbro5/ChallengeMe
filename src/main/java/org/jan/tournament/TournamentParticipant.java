package org.jan.tournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

@Entity
@Table(name = "tournament_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class TournamentParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 1-based seed assigned when tournament starts */
    private int seed = 0;

    private boolean eliminated = false;

    public TournamentParticipant(Tournament tournament, User user) {
        this.tournament = tournament;
        this.user       = user;
    }
}
