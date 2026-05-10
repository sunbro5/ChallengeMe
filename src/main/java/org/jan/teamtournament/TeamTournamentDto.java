package org.jan.teamtournament;

import java.time.LocalDateTime;
import java.util.List;

public record TeamTournamentDto(
    Long id,
    String name,
    String gameType,
    int teamCapacity,
    int teamSizeMax,
    String format,
    String status,
    String creatorUsername,
    String winnerTeamName,
    LocalDateTime createdAt,
    int teamCount,
    List<TeamDto> teams,
    List<MatchDto> matches
) {
    public record MemberDto(Long userId, String username) {}

    public record TeamDto(
        Long id,
        String name,
        String captainUsername,
        int seed,
        boolean eliminated,
        int points,
        int wins,
        int draws,
        int losses,
        List<MemberDto> members
    ) {}

    public record MatchDto(
        Long id,
        int round,
        int matchIndex,
        String teamAName,
        String teamBName,
        String winnerTeamName,
        boolean draw,
        String status,
        String note
    ) {}
}
