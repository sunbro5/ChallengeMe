package org.jan.game.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class GameEventDto {
    Long id;
    String creatorUsername;
    double latitude;
    double longitude;
    String gameType;
    LocalDateTime scheduledAt;
    String status;
    List<String> participants;
    boolean joined;
    // Note: boolean fields starting with "is" get isIsXxx() getter in Lombok,
    // which Jackson serializes as "isXxx" — exactly what the frontend expects.
    // Boolean (boxed) so Lombok generates getIsCreator()/getIsChallenger()
    // → Jackson serializes as "isCreator"/"isChallenger" (strips "get", not "is")
    Boolean isCreator;
    Boolean isChallenger;
    String challengerUsername;
    boolean creatorResultSubmitted;
    boolean challengerResultSubmitted;
    boolean iHaveSubmitted;
    String winnerUsername;
    String resultNote;
    /** Only populated for DISPUTED events. */
    String creatorResult;
    String challengerResult;
    /** Optional free-text from the creator, e.g. skill level or location details. */
    String description;
}
