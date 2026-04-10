package org.jan.chat;

import jakarta.servlet.http.HttpSession;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    private User currentUser(HttpSession session) {
        User s = (User) session.getAttribute("user");
        return s == null ? null : userRepository.findById(s.getId()).orElse(null);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam String friendUsername, HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        User friend = userRepository.findByUsername(friendUsername);
        if (friend == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(chatService.getConversation(user, friend).stream()
                .map(m -> Map.<String, Object>of(
                        "senderUsername", m.getSender().getUsername(),
                        "receiverUsername", m.getReceiver().getUsername(),
                        "content", m.getContent(),
                        "sentAt", m.getSentAt().toString()))
                .collect(Collectors.toList()));
    }
}
