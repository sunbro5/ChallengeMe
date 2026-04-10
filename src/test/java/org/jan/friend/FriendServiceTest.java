package org.jan.friend;

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
class FriendServiceTest extends BaseIntegrationTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = userService.registerUser("alice_f", "pass123", "alice_f@example.com");
        bob   = userService.registerUser("bob_f",   "pass123", "bob_f@example.com");
        // Reload to get managed entities
        alice = userRepository.findByUsername("alice_f");
        bob   = userRepository.findByUsername("bob_f");
    }

    @Test
    void sendRequest_success_createsPendingFriendship() {
        Friendship f = friendService.sendRequest(alice, "bob_f");
        assertNotNull(f.getId());
        assertEquals(FriendshipStatus.PENDING, f.getStatus());
        assertEquals(alice.getId(), f.getRequester().getId());
        assertEquals(bob.getId(), f.getAddressee().getId());
    }

    @Test
    void sendRequest_unknownUser_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> friendService.sendRequest(alice, "nobody"));
    }

    @Test
    void sendRequest_toSelf_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> friendService.sendRequest(alice, "alice_f"));
    }

    @Test
    void sendRequest_duplicate_throws() {
        friendService.sendRequest(alice, "bob_f");
        assertThrows(IllegalArgumentException.class,
                () -> friendService.sendRequest(alice, "bob_f"));
    }

    @Test
    void acceptRequest_success_updatesStatusToAccepted() {
        Friendship f = friendService.sendRequest(alice, "bob_f");
        Friendship accepted = friendService.acceptRequest(bob, f.getId());
        assertEquals(FriendshipStatus.ACCEPTED, accepted.getStatus());
    }

    @Test
    void acceptRequest_wrongUser_throws() {
        Friendship f = friendService.sendRequest(alice, "bob_f");
        assertThrows(IllegalArgumentException.class,
                () -> friendService.acceptRequest(alice, f.getId())); // alice is requester, not addressee
    }

    @Test
    void getFriends_afterAccept_returnsEachOther() {
        Friendship f = friendService.sendRequest(alice, "bob_f");
        friendService.acceptRequest(bob, f.getId());

        List<User> aliceFriends = friendService.getFriends(alice);
        List<User> bobFriends   = friendService.getFriends(bob);

        assertEquals(1, aliceFriends.size());
        assertEquals("bob_f", aliceFriends.get(0).getUsername());
        assertEquals(1, bobFriends.size());
        assertEquals("alice_f", bobFriends.get(0).getUsername());
    }

    @Test
    void getFriends_beforeAccept_returnsEmpty() {
        friendService.sendRequest(alice, "bob_f");
        assertTrue(friendService.getFriends(alice).isEmpty());
    }

    @Test
    void getPendingRequests_returnsIncomingRequests() {
        friendService.sendRequest(alice, "bob_f");
        List<Friendship> pending = friendService.getPendingRequests(bob);
        assertEquals(1, pending.size());
        assertEquals(alice.getId(), pending.get(0).getRequester().getId());
    }

    @Test
    void getPendingRequests_forRequester_returnsEmpty() {
        friendService.sendRequest(alice, "bob_f");
        assertTrue(friendService.getPendingRequests(alice).isEmpty());
    }
}
