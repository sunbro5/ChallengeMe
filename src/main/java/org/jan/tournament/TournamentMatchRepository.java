package org.jan.tournament;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {

    List<TournamentMatch> findByTournamentOrderByRoundAscMatchIndexAsc(Tournament tournament);

    List<TournamentMatch> findByTournamentAndRound(Tournament tournament, int round);
}
