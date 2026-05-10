package org.jan.teamtournament;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TournamentTeamMemberRepository extends JpaRepository<TournamentTeamMember, Long> {
    List<TournamentTeamMember> findByTeam(TournamentTeam team);
    int countByTeam(TournamentTeam team);
    boolean existsByTeamAndUser(TournamentTeam team, User user);

    /** Check if a user is already in any team in this tournament. */
    @Query("SELECT COUNT(m) > 0 FROM TournamentTeamMember m WHERE m.team.tournament = :tournament AND m.user = :user")
    boolean existsByTournamentAndUser(TeamTournament tournament, User user);

    /** Load all members for multiple teams at once (batch). */
    @Query("SELECT m FROM TournamentTeamMember m WHERE m.team IN :teams")
    List<TournamentTeamMember> findByTeamIn(List<TournamentTeam> teams);
}
