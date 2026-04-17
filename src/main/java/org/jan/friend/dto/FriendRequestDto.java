package org.jan.friend.dto;

import lombok.Value;

@Value
public class FriendRequestDto {
    Long id;
    Long requesterId;
    String requesterUsername;
}
