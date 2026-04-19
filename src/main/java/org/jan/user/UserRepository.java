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

    /** Top players ordered by ELO rating desc, then wins desc. */
    @Query("SELECT u FROM User u WHERE u.role = 'USER' ORDER BY u.rating DESC, u.wins DESC")
    List<User> findLeaderboard();

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
