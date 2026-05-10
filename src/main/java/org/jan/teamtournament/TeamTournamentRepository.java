package org.jan.teamtournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TeamTournamentRepository extends JpaRepository<TeamTournament, Long> {
    @Query("SELECT t FROM TeamTournament t WHERE t.status IN ('OPEN', 'IN_PROGRESS') ORDER BY t.createdAt DESC")
    List<TeamTournament> findActive();
}
