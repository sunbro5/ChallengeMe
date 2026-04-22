package org.jan.sportsmanship;

import org.jan.game.GameEvent;
import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SportsmanshipRepository extends JpaRepository<Sportsmanship, Long> {

    boolean existsByVoterAndEvent(User voter, GameEvent event);

    long countByTargetAndPositive(User target, boolean positive);

    @Query("SELECT s FROM Sportsmanship s WHERE s.voter = :voter AND s.event = :event")
    java.util.Optional<Sportsmanship> findByVoterAndEvent(
            @Param("voter") User voter, @Param("event") GameEvent event);
}
