package org.jan.game;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByEvent(GameEvent event);

    /**
     * Load all team members for a batch of events in one query.
     * Use in list endpoints instead of calling findByEvent() per event (N+1 prevention).
     */
    @Query("SELECT m FROM TeamMember m JOIN FETCH m.user WHERE m.event IN :events")
    List<TeamMember> findByEventIn(@Param("events") List<GameEvent> events);

    List<TeamMember> findByEventAndSide(GameEvent event, String side);

    boolean existsByEventAndUser(GameEvent event, User user);

    Optional<TeamMember> findByEventAndUser(GameEvent event, User user);

    long countByEventAndSide(GameEvent event, String side);
}
