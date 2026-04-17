package org.jan.user.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class GameHistoryDto {
    Long id;
    String gameType;
    LocalDateTime scheduledAt;
    String status;
    String opponentUsername;
    /** "won" | "lost" | "draw" | "disputed" */
    String result;
    String resultNote;
    // Only populated for DISPUTED events
    String creatorUsername;
    String creatorResult;
    String challengerResult;
}
