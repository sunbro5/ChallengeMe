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

    /** All events where the user is any participant, newest scheduled first. */
    @Query("SELECT e FROM GameEvent e JOIN e.participants p WHERE p = :user ORDER BY e.scheduledAt DESC")
    List<GameEvent> findAllByParticipantOrderByScheduledAtDesc(@Param("user") User user);

    /** OPEN events whose scheduledAt is before the given cutoff (stale open). */
    @Query("SELECT e FROM GameEvent e WHERE e.status = 'OPEN' AND e.scheduledAt < :cutoff")
    List<GameEvent> findStaleOpen(@Param("cutoff") LocalDateTime cutoff);

    /** IN_PROGRESS events whose scheduledAt is before the given cutoff (never finished). */
    @Query("SELECT e FROM GameEvent e WHERE e.status = 'IN_PROGRESS' AND e.scheduledAt < :cutoff")
    List<GameEvent> findStaleInProgress(@Param("cutoff") LocalDateTime cutoff);

    /** FINISHED events of friends, newest first — for the activity feed. */
    @Query("SELECT e FROM GameEvent e JOIN e.participants p " +
           "WHERE e.status = 'FINISHED' AND p IN :friends " +
           "ORDER BY e.scheduledAt DESC")
    List<GameEvent> findFinishedByParticipantsIn(
            @Param("friends") List<User> friends,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(e) FROM GameEvent e " +
           "WHERE e.creator = :creator AND e.createdAt >= :from")
    long countByCreatorSince(
            @Param("creator") User creator,
            @Param("from")    LocalDateTime from);

    /** All FINISHED events of a given game type (for per-game leaderboard). */
    @Query("SELECT DISTINCT e FROM GameEvent e JOIN FETCH e.participants " +
           "WHERE e.gameType = :gameType AND e.status = 'FINISHED'")
    List<GameEvent> findFinishedByGameType(@Param("gameType") GameType gameType);

    /**
     * All events where the given user is a participant and the status is one of
     * the supplied values, newest first.  Uses JOIN FETCH to avoid N+1 on participants.
     */
    @Query("SELECT DISTINCT e FROM GameEvent e " +
           "JOIN FETCH e.participants " +
           "JOIN e.participants p " +
           "WHERE p = :user AND e.status IN :statuses " +
           "ORDER BY e.scheduledAt DESC")
    List<GameEvent> findByParticipantAndStatusIn(
            @Param("user")     User user,
            @Param("statuses") List<EventStatus> statuses);
}
