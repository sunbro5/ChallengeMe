package org.jan.chat;

import org.jan.BaseIntegrationTest;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.jan.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ChatServiceTest extends BaseIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        userService.registerUser("alice_c", "pass123", "alice_c@example.com");
        userService.registerUser("bob_c",   "pass123", "bob_c@example.com");
        alice = userRepository.findByUsername("alice_c");
        bob   = userRepository.findByUsername("bob_c");
    }

    @Test
    void saveMessage_success_persistsMessage() {
        Message msg = chatService.saveMessage("alice_c", "bob_c", "Hello Bob!");
        assertNotNull(msg.getId());
        assertEquals("alice_c", msg.getSender().getUsername());
        assertEquals("bob_c", msg.getReceiver().getUsername());
        assertEquals("Hello Bob!", msg.getContent());
        assertNotNull(msg.getSentAt());
    }

    @Test
    void saveMessage_unknownSender_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> chatService.saveMessage("nobody", "bob_c", "Hi"));
    }

    @Test
    void saveMessage_unknownReceiver_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> chatService.saveMessage("alice_c", "nobody", "Hi"));
    }

    @Test
    void getConversation_returnsMessagesInOrder() {
        chatService.saveMessage("alice_c", "bob_c", "First");
        chatService.saveMessage("bob_c", "alice_c", "Second");
        chatService.saveMessage("alice_c", "bob_c", "Third");

        List<Message> conv = chatService.getConversation(alice, bob);
        assertEquals(3, conv.size());
        assertEquals("First",  conv.get(0).getContent());
        assertEquals("Second", conv.get(1).getContent());
        assertEquals("Third",  conv.get(2).getContent());
    }

    @Test
    void getConversation_excludesUnrelatedMessages() {
        userService.registerUser("carol_c", "pass123", "carol_c@example.com");
        chatService.saveMessage("alice_c", "bob_c", "Hello");
        chatService.saveMessage("carol_c", "bob_c", "Hi Bob from Carol");

        User carol = userRepository.findByUsername("carol_c");
        List<Message> aliceBobConv = chatService.getConversation(alice, bob);
        List<Message> carolBobConv = chatService.getConversation(carol, bob);

        assertEquals(1, aliceBobConv.size());
        assertEquals(1, carolBobConv.size());
    }

    @Test
    void getConversation_emptyWhenNoMessages() {
        List<Message> conv = chatService.getConversation(alice, bob);
        assertTrue(conv.isEmpty());
    }
}
