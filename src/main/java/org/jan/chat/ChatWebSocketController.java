package org.jan.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message, Principal principal) {
        if (principal == null) return;
        message.setSenderUsername(principal.getName());
        message.setSentAt(LocalDateTime.now());
        chatService.saveMessage(message.getSenderUsername(), message.getReceiverUsername(), message.getContent());
        messagingTemplate.convertAndSendToUser(message.getReceiverUsername(), "/queue/messages", message);
    }
}
