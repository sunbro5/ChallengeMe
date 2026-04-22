package org.jan.tournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.game.GameEvent;
import org.jan.user.User;

@Entity
@Table(name = "tournament_matches")
@Getter
@Setter
@NoArgsConstructor
public class TournamentMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    /** 1 = first round (e.g. semis for 4-player), 2 = final, etc. */
    private int round;

    /** 0-based index within the round */
    private int matchIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id")
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private User player2;

    /** The on-map challenge created for this match */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_event_id")
    private GameEvent gameEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    public TournamentMatch(Tournament tournament, int round, int matchIndex, User player1, User player2) {
        this.tournament  = tournament;
        this.round       = round;
        this.matchIndex  = matchIndex;
        this.player1     = player1;
        this.player2     = player2;
    }
}
