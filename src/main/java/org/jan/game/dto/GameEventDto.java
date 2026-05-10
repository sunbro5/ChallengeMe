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
    /** Optional human-readable venue name. */
    String locationName;
    /** If set, only this username can accept (direct invite). */
    String invitedUsername;

    // ── Team quiz fields (only populated for PUB_QUIZ events) ────────────────
    /** True when gameType == PUB_QUIZ — enables the team UI on the frontend. */
    Boolean isTeamEvent;
    /** Creator's username + all CREATOR-side team members. */
    List<String> creatorTeam;
    /** Challenger's username + all CHALLENGER-side team members (null if no challenger yet). */
    List<String> challengerTeam;
    /**
     * Which side the viewing user is on: "CREATOR", "CHALLENGER", or null.
     * Captains also get this field set so the frontend knows not to offer a join button.
     */
    String myTeamSide;

    // ── Visibility ────────────────────────────────────────────────────────────
    /** Who can see this event: PUBLIC, FRIENDS, PRIVATE, TOURNAMENT. */
    String visibility;
    /** True when the viewing user is an accepted friend of the creator. Used for the friends-only map filter. */
    Boolean creatorIsFriend;
}
