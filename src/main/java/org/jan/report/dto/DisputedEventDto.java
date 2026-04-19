package org.jan.report.dto;

public record DisputedEventDto(
        Long id,
        String gameType,
        String creatorUsername,
        String challengerUsername,
        String creatorResult,
        String challengerResult,
        String scheduledAt
) {}
