package org.jan.game;

import org.jan.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GameEventRepository extends JpaRepository<GameEvent, Long> {

    List<GameEvent> findByStatus(EventStatus status);

    /**
     * Load events by status with all participants fetched in a single JOIN — avoids
     * LazyInitializationException when toDto() accesses participants after the transaction closes.
     */
    @Query("SELECT DISTINCT e FROM GameEvent e JOIN FETCH e.participants WHERE e.status IN :statuses")
    List<GameEvent> findByStatusIn(@Param("statuses") List<EventStatus> statuses);

    /** 200 newest OPEN events for the public home-page cache. */
    List<GameEvent> findTop200ByStatusOrderByCreatedAtDesc(EventStatus status);

    /** Count events created by the given user within a time window. */
    List<GameEvent> findByCreator(User creator);

    List<GameEvent> findByCreatorOrderByScheduledAtDesc(User creator);

    @Query("SELECT e FROM GameEvent e WHERE e.winner = :user")
    List<GameEvent> findByWinner(@Param("user") User user);

    /**
     * Load a single event with all participants fetched in one query.
     * Use this instead of findById() whenever toDto() will be called after the transaction closes.
     */
    @Query("SELECT DISTINCT e FROM GameEvent e JOIN FETCH e.participants WHERE e.id = :id")
    java.util.Optional<GameEvent> findByIdWithParticipants(@Param("id") Long id);

    @Query("SELECT e FROM GameEvent e JOIN e.participants p WHERE p = :user")
    List<GameEvent> findByParticipantsContaining(@Param("user") User user);

    /**
     * All events where the user is any participant, newest scheduled first.
     * Uses JOIN FETCH so participants are loaded in one query — safe to use
     * outside a transaction (e.g. inside toDto in the controller layer).
     */
    @Query("SELECT DISTINCT e FROM GameEvent e " +
           "JOIN FETCH e.participants " +
           "JOIN e.participants p " +
           "WHERE p = :user ORDER BY e.scheduledAt DESC")
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

    /**
     * Count FINISHED 1v1 events involving both users since the given cutoff.
     * Used to compute the ELO diminishing-returns multiplier for repeated opponents.
     * Note: called before the current event is persisted as FINISHED, so the count
     * reflects only *prior* matches this window.
     */
    @Query("SELECT COUNT(DISTINCT e) FROM GameEvent e " +
           "JOIN e.participants p1 JOIN e.participants p2 " +
           "WHERE p1 = :a AND p2 = :b " +
           "AND e.status = 'FINISHED' AND e.scheduledAt >= :since")
    long countRecentFinishedBetween(
            @Param("a")     User a,
            @Param("b")     User b,
            @Param("since") LocalDateTime since);

    /**
     * Recent FINISHED events — used by the admin fraud panel to detect pairs that play
     * each other suspiciously often.  Capped at {@code pageable.pageSize} rows (use
     * {@code PageRequest.of(0, 500)}) to prevent unbounded memory load.
     */
    @Query("SELECT DISTINCT e FROM GameEvent e JOIN FETCH e.participants " +
           "WHERE e.status = 'FINISHED' AND e.scheduledAt >= :since " +
           "ORDER BY e.scheduledAt DESC")
    List<GameEvent> findFinishedSince(@Param("since") LocalDateTime since, Pageable pageable);
}
