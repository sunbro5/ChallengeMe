package org.jan.achievement;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<UserAchievement, Long> {
    boolean existsByUserAndAchievementType(User user, AchievementType type);
    List<UserAchievement> findByUserOrderByAwardedAtAsc(User user);
}
