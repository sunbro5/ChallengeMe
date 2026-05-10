package org.jan.teamtournament;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamTournamentMatchRepository extends JpaRepository<TeamTournamentMatch, Long> {
    List<TeamTournamentMatch> findByTournamentOrderByRoundAscMatchIndexAsc(TeamTournament tournament);
    List<TeamTournamentMatch> findByTournamentAndRound(TeamTournament tournament, int round);
}
