package org.jan.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /** All non-finished tournaments (OPEN or IN_PROGRESS), newest first */
    @Query("SELECT t FROM Tournament t WHERE t.status IN ('OPEN', 'IN_PROGRESS') ORDER BY t.createdAt DESC")
    List<Tournament> findActive();

    /** All tournaments including finished, newest first (for history) */
    @Query("SELECT t FROM Tournament t ORDER BY t.createdAt DESC")
    List<Tournament> findAllOrderByCreatedAtDesc();
}
