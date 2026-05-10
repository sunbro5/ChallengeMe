package org.jan.teamtournament;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
    List<TournamentTeam> findByTournament(TeamTournament tournament);
    List<TournamentTeam> findByTournamentOrderByPointsDescWinsDesc(TeamTournament tournament);
    int countByTournament(TeamTournament tournament);
    boolean existsByTournamentAndName(TeamTournament tournament, String name);
}
