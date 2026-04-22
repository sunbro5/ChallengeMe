package org.jan.tournament;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {

    List<TournamentParticipant> findByTournamentOrderBySeedAsc(Tournament tournament);

    boolean existsByTournamentAndUser(Tournament tournament, User user);

    int countByTournament(Tournament tournament);
}
