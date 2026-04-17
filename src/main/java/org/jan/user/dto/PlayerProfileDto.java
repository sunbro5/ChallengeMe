package org.jan.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PlayerProfileDto {
    Long id;
    String username;
    int wins;
    int losses;
    int draws;
    int disputes;
    String friendStatus;
    Boolean isMe;   // Boolean (boxed) → Lombok generates getIsMe() → Jackson serializes as "isMe"
    List<GameHistoryDto> games;
}
