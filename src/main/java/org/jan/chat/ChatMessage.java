package org.jan.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChatMessage {
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private LocalDateTime sentAt;
}
