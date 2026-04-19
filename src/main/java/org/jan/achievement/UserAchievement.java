package org.jan.achievement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_type"}))
@Getter @Setter @NoArgsConstructor
public class UserAchievement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false)
    private AchievementType achievementType;

    private LocalDateTime awardedAt = LocalDateTime.now();

    public UserAchievement(User user, AchievementType achievementType) {
        this.user            = user;
        this.achievementType = achievementType;
        this.awardedAt       = LocalDateTime.now();
    }
}
