package org.jan.chat.dto;

import lombok.Value;

@Value
public class ChatMessageDto {
    String senderUsername;
    String receiverUsername;
    String content;
    String sentAt;
}
