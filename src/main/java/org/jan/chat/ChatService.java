package org.jan.chat;

import org.jan.config.ProfanityFilter;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository    userRepository;
    @Autowired private ProfanityFilter   profanityFilter;

    public Message saveMessage(String senderUsername, String receiverUsername, String content) {
        User sender = userRepository.findByUsername(senderUsername);
        User receiver = userRepository.findByUsername(receiverUsername);
        if (sender == null || receiver == null) throw new IllegalArgumentException("User not found");
        profanityFilter.check(content);
        return messageRepository.save(new Message(sender, receiver, content));
    }

    public List<Message> getConversation(User user1, User user2) {
        return messageRepository.findConversation(user1, user2);
    }
}
