package org.jan.friend.dto;

import lombok.Value;

@Value
public class UserSearchDto {
    Long id;
    String username;
    String friendStatus;
}
