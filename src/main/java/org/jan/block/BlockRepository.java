package org.jan.block;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    @Query("SELECT COUNT(b) > 0 FROM Block b WHERE " +
           "(b.blocker = :a AND b.blocked = :b) OR (b.blocker = :b AND b.blocked = :a)")
    boolean existsBlockBetween(@Param("a") User a, @Param("b") User b);
}
