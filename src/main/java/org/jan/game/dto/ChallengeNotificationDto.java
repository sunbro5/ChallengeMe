package org.jan.game.dto;

import lombok.Value;

@Value
public class ChallengeNotificationDto {
    String type;
    Long eventId;
    String challengerUsername;
    String gameType;
}
