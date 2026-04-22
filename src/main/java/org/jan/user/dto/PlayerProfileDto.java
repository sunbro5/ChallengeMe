package org.jan.user.dto;

import lombok.Builder;
import lombok.Value;
import org.jan.achievement.AchievementDto;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class PlayerProfileDto {
    Long id;
    String username;
    String bio;
    String favoriteGameKey;
    int wins;
    int losses;
    int draws;
    int disputes;
    int rating;
    String friendStatus;
    Boolean isMe;   // Boolean (boxed) → Lombok generates getIsMe() → Jackson serializes as "isMe"
    List<GameHistoryDto> games;
    List<GameTypeStatsDto> gameStats;
    List<AchievementDto> achievements;
    int thumbsUp;
    int thumbsDown;
    LocalDateTime memberSince;
    Integer reputationScore;  // 1–5, null if fewer than 5 games
}
