package org.jan.chat;

import org.jan.chat.dto.ChatMessageDto;
import org.jan.chat.dto.SendMessageRequest;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired private ChatService            chatService;
    @Autowired private UserRepository         userRepository;
    @Autowired private SimpMessagingTemplate  messagingTemplate;

    /**
     * REST send — saves the message and pushes it over WebSocket to the receiver's
     * personal topic so their ChatPopup / EventDetailPage updates in real time.
     */
    @Transactional
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @RequestBody SendMessageRequest req, Authentication auth) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (req.getContent().length() > 1000) {
            return ResponseEntity.badRequest().build();
        }

        String senderUsername = auth.getName();
        chatService.saveMessage(senderUsername, req.getReceiverUsername(), req.getContent());

        ChatMessageDto dto = new ChatMessageDto(
                senderUsername,
                req.getReceiverUsername(),
                req.getContent(),
                LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/chat/" + req.getReceiverUsername(), dto);
        return ResponseEntity.ok(dto);
    }

    @Transactional(readOnly = true)
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDto>> getHistory(
            @RequestParam String friendUsername, Authentication auth) {
        User user   = userRepository.findByUsername(auth.getName());
        User friend = userRepository.findByUsername(friendUsername);
        if (friend == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatMessageDto> history = chatService.getConversation(user, friend).stream()
                .map(m -> new ChatMessageDto(
                        m.getSender().getUsername(),
                        m.getReceiver().getUsername(),
                        m.getContent(),
                        m.getSentAt().toString()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }
}
