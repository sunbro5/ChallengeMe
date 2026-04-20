package org.jan.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByEmailIgnoreCase(String email);
    /** Returns at most 20 matching users — DB-level limit avoids loading thousands. */
    List<User> findTop20ByUsernameContainingIgnoreCaseAndRoleNot(String query, String role);

    /**
     * Top players ordered by ELO desc, wins desc.
     * Minimum 5 completed games — filters out throwaway / ELO-farming accounts.
     */
    @Query("SELECT u FROM User u WHERE u.role = 'USER' " +
           "AND (u.wins + u.losses + u.draws) >= 5 " +
           "ORDER BY u.rating DESC, u.wins DESC")
    List<User> findLeaderboard();

    /**
     * Users eligible for the admin fraud panel: active (not banned), role=USER,
     * at least 5 completed games, and a dispute rate exceeding 25 %.
     * JPQL arithmetic: disputes/total > 0.25  ↔  disputes * 4 > total.
     */
    @Query("SELECT u FROM User u WHERE u.role = 'USER' AND u.banned = false " +
           "AND (u.wins + u.losses + u.draws) >= 5 " +
           "AND u.disputes * 4 > (u.wins + u.losses + u.draws)")
    List<User> findHighDisputeUsers();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.wins     = u.wins     + 1 WHERE u.id IN :ids")
    void bulkIncrementWins(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.losses   = u.losses   + 1 WHERE u.id IN :ids")
    void bulkIncrementLosses(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.draws    = u.draws    + 1 WHERE u.id IN :ids")
    void bulkIncrementDraws(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.disputes = u.disputes + 1 WHERE u.id IN :ids")
    void bulkIncrementDisputes(@Param("ids") List<Long> ids);
}
