package org.jan.achievement;

import java.time.LocalDateTime;

public record AchievementDto(
        String type,
        LocalDateTime awardedAt
) {}
