package org.jan.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByUsernameContainingIgnoreCaseAndRoleNot(String query, String role);

    /** Top players ordered by wins desc, then losses asc. */
    @Query("SELECT u FROM User u WHERE u.role = 'USER' ORDER BY u.wins DESC, u.losses ASC")
    List<User> findLeaderboard();
}
