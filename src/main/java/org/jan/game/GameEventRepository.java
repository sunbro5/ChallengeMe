package org.jan.game;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GameEventRepository extends JpaRepository<GameEvent, Long> {

    List<GameEvent> findByStatus(EventStatus status);

    List<GameEvent> findByStatusIn(List<EventStatus> statuses);

    /** 200 newest OPEN events for the public home-page cache. */
    List<GameEvent> findTop200ByStatusOrderByCreatedAtDesc(EventStatus status);

    /** Count events created by the given user within a time window. */
    List<GameEvent> findByCreator(User creator);

    List<GameEvent> findByCreatorOrderByScheduledAtDesc(User creator);

    @Query("SELECT e FROM GameEvent e WHERE e.winner = :user")
    List<GameEvent> findByWinner(@Param("user") User user);

    @Query("SELECT e FROM GameEvent e JOIN e.participants p WHERE p = :user")
    List<GameEvent> findByParticipantsContaining(@Param("user") User user);

    @Query("SELECT COUNT(e) FROM GameEvent e " +
           "WHERE e.creator = :creator AND e.createdAt >= :from")
    long countByCreatorSince(
            @Param("creator") User creator,
            @Param("from")    LocalDateTime from);

    /**
     * All events where the given user is a participant and the status is one of
     * the supplied values, newest first.
     */
    @Query("SELECT e FROM GameEvent e JOIN e.participants p " +
           "WHERE p = :user AND e.status IN :statuses " +
           "ORDER BY e.scheduledAt DESC")
    List<GameEvent> findByParticipantAndStatusIn(
            @Param("user")     User user,
            @Param("statuses") List<EventStatus> statuses);
}
