package org.jan.friend.dto;

import java.time.LocalDateTime;

/**
 * One entry in the friends activity feed.
 *
 * @param eventId        the game event id
 * @param friendUsername the friend who played (subject of the sentence)
 * @param opponent       their opponent's username
 * @param outcome        "won" | "lost" | "draw" (from the friend's perspective)
 * @param gameType       e.g. "CHESS"
 * @param playedAt       when the game was scheduled
 */
public record ActivityDto(
        Long          eventId,
        String        friendUsername,
        String        opponent,
        String        outcome,
        String        gameType,
        LocalDateTime playedAt
) {}
