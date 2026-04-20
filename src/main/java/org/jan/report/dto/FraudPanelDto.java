package org.jan.report.dto;

import java.util.List;

/**
 * Response for GET /api/admin/fraud.
 * Contains two sections:
 *   - suspiciousPairs:   player pairs who played each other unusually often this week
 *   - highDisputeUsers:  players whose dispute rate exceeds the warning threshold
 */
public record FraudPanelDto(
        List<SuspiciousPairDto> suspiciousPairs,
        List<HighDisputeUserDto> highDisputeUsers
) {
    public record SuspiciousPairDto(String userA, String userB, long matchCount) {}

    public record HighDisputeUserDto(
            Long   userId,
            String username,
            boolean banned,
            int    wins,
            int    losses,
            int    draws,
            int    disputes,
            int    totalGames,
            double disputeRate   // 0.0–1.0
    ) {}
}
