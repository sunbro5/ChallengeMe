package org.jan.user.dto;

public record GameTypeStatsDto(
        String gameType,
        int wins,
        int losses,
        int draws,
        int disputes
) {}
