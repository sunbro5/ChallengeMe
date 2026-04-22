package org.jan.tournament;

import java.time.LocalDateTime;
import java.util.List;

public record TournamentDto(
        Long id,
        String name,
        String gameType,
        int capacity,
        String status,
        String creatorUsername,
        String winnerUsername,
        LocalDateTime createdAt,
        int participantCount,
        List<ParticipantDto> participants,
        List<MatchDto> matches
) {
    public record ParticipantDto(
            Long userId,
            String username,
            int seed,
            boolean eliminated
    ) {}

    public record MatchDto(
            Long id,
            int round,
            int matchIndex,
            String player1Username,
            String player2Username,
            Long gameEventId,
            String winnerUsername
    ) {}
}
