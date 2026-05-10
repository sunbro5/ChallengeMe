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
        // Resolve sender: prefer the authenticated Principal, fall back to the
        // senderUsername field that the client includes in the payload.
        String sender = (principal != null) ? principal.getName() : message.getSenderUsername();
        if (sender == null || sender.isBlank()) return;

        message.setSenderUsername(sender);
        message.setSentAt(LocalDateTime.now());
        chatService.saveMessage(sender, message.getReceiverUsername(), message.getContent());

        // Deliver to the receiver's personal topic.
        // The client subscribes to /topic/chat/{ownUsername} on connect.
        messagingTemplate.convertAndSend("/topic/chat/" + message.getReceiverUsername(), message);
    }
}
