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
}
