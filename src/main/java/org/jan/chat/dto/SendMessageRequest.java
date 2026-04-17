package org.jan.chat.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String receiverUsername;
    private String content;
}
