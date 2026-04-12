package org.jan.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByKey(String key);
    boolean existsByKey(String key);
}
