package org.jan.game;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameEventCreateRequest {
    private double latitude;
    private double longitude;
    private GameType gameType;
    private LocalDateTime scheduledAt;
    private String description;
    private String locationName;
    private String invitedUsername;
    /** True when the creator wants to play with a team (only valid for team-eligible game types). */
    private boolean teamMode;

    /**
     * Who can see/accept this event.
     * Accepted values: PUBLIC (default), FRIENDS, PRIVATE.
     * TOURNAMENT is reserved for the tournament system and cannot be set manually.
     */
    private String visibility;
}
