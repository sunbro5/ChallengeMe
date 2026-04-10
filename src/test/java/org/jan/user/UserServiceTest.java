package org.jan.user;

import org.jan.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUser_success() {
        User user = userService.registerUser("alice", "pass123", "alice@example.com");
        assertNotNull(user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("USER", user.getRole());
        assertFalse(user.isBanned());
    }

    @Test
    void registerUser_duplicateUsername_throws() {
        userService.registerUser("bob", "pass123", "bob@example.com");
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("bob", "pass123", "bob2@example.com"));
    }

    @Test
    void registerUser_duplicateEmail_throws() {
        userService.registerUser("charlie", "pass123", "shared@example.com");
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("charlie2", "pass123", "shared@example.com"));
    }

    @Test
    void registerUser_shortPassword_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("dave", "123", "dave@example.com"));
    }

    @Test
    void registerUser_invalidEmail_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("eve", "pass123", "notanemail"));
    }

    @Test
    void registerUser_emptyUsername_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("  ", "pass123", "x@x.com"));
    }

    @Test
    void authenticateUser_correctCredentials_returnsUser() {
        userService.registerUser("frank", "pass123", "frank@example.com");
        User result = userService.authenticateUser("frank", "pass123");
        assertNotNull(result);
        assertEquals("frank", result.getUsername());
    }

    @Test
    void authenticateUser_wrongPassword_returnsNull() {
        userService.registerUser("grace", "pass123", "grace@example.com");
        assertNull(userService.authenticateUser("grace", "wrong"));
    }

    @Test
    void authenticateUser_unknownUser_returnsNull() {
        assertNull(userService.authenticateUser("nobody", "pass123"));
    }

    @Test
    void authenticateUser_bannedUser_throws() {
        userService.registerUser("henry", "pass123", "henry@example.com");
        User user = userRepository.findByUsername("henry");
        user.setBanned(true);
        userRepository.save(user);

        assertThrows(IllegalArgumentException.class,
                () -> userService.authenticateUser("henry", "pass123"));
    }
}
