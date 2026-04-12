package org.jan.game;

import org.jan.BaseIntegrationTest;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.jan.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class GameEventServiceTest extends BaseIntegrationTest {

    @Autowired private GameEventService gameEventService;
    @Autowired private UserService      userService;
    @Autowired private UserRepository   userRepository;

    private User alice;
    private User bob;

    private static final LocalDateTime FUTURE = LocalDateTime.now().plusDays(1);

    @BeforeEach
    void setUp() {
        userService.registerUser("alice_gs", "pass123", "alice_gs@test.com");
        userService.registerUser("bob_gs",   "pass123", "bob_gs@test.com");
        alice = userRepository.findByUsername("alice_gs");
        bob   = userRepository.findByUsername("bob_gs");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Full flow: create → accept → approve → returns IN_PROGRESS event. */
    private GameEvent createAndApprove() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId());
        return gameEventService.approveChallenger(alice, e.getId());
    }

    // ── createEvent ──────────────────────────────────────────────────────────

    @Test
    void createEvent_isOpenAndCreatorIsParticipant() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        assertEquals(EventStatus.OPEN, e.getStatus());
        assertEquals(alice.getId(), e.getCreator().getId());
        assertEquals(1, e.getParticipants().size());
        assertTrue(gameEventService.getActiveEvents().stream()
                .anyMatch(ev -> ev.getId().equals(e.getId())));
    }

    // ── createEvent — daily limit ────────────────────────────────────────────

    @Test
    void createEvent_upToLimit_allSucceed() {
        for (int i = 0; i < GameEventService.DAILY_EVENT_LIMIT; i++) {
            GameEvent e = gameEventService.createEvent(alice, 51.5 + i, -0.1, GameType.TIC_TAC_TOE, FUTURE);
            assertNotNull(e.getId());
        }
    }

    @Test
    void createEvent_overLimit_throws() {
        for (int i = 0; i < GameEventService.DAILY_EVENT_LIMIT; i++) {
            gameEventService.createEvent(alice, 51.5 + i, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        }
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> gameEventService.createEvent(alice, 60.0, -0.1, GameType.TIC_TAC_TOE, FUTURE));
        assertTrue(ex.getMessage().contains(String.valueOf(GameEventService.DAILY_EVENT_LIMIT)));
    }

    @Test
    void createEvent_limitIsPerUser_otherUserUnaffected() {
        for (int i = 0; i < GameEventService.DAILY_EVENT_LIMIT; i++) {
            gameEventService.createEvent(alice, 51.5 + i, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        }
        // Alice is at limit — bob should still be able to create
        GameEvent e = gameEventService.createEvent(bob, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        assertNotNull(e.getId());
        assertEquals(EventStatus.OPEN, e.getStatus());
    }

    // ── acceptChallenge → PENDING_APPROVAL ───────────────────────────────────

    @Test
    void acceptChallenge_moveTosPendingApproval() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        GameEvent e2 = gameEventService.acceptChallenge(bob, e.getId());
        assertEquals(EventStatus.PENDING_APPROVAL, e2.getStatus());
        assertEquals(2, e2.getParticipants().size());
        assertEquals(bob.getId(), e2.getChallenger().getId());
    }

    @Test
    void acceptChallenge_byCreator_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.acceptChallenge(alice, e.getId()));
    }

    @Test
    void acceptChallenge_eventNotOpen_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId()); // now PENDING_APPROVAL

        User charlie = userService.registerUser("charlie_gs", "pass123", "charlie_gs@test.com");
        charlie = userRepository.findByUsername("charlie_gs");
        User finalC = charlie;
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.acceptChallenge(finalC, e.getId()));
    }

    @Test
    void acceptChallenge_unknownEvent_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.acceptChallenge(bob, 999_999L));
    }

    // ── approveChallenger → IN_PROGRESS ──────────────────────────────────────

    @Test
    void approveChallenger_byCreator_movesToInProgress() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId());
        GameEvent approved = gameEventService.approveChallenger(alice, e.getId());
        assertEquals(EventStatus.IN_PROGRESS, approved.getStatus());
    }

    @Test
    void approveChallenger_byNonCreator_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId());
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.approveChallenger(bob, e.getId()));
    }

    @Test
    void approveChallenger_whenNotPendingApproval_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        // Still OPEN — no challenger yet
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.approveChallenger(alice, e.getId()));
    }

    // ── rejectChallenger → back to OPEN ──────────────────────────────────────

    @Test
    void rejectChallenger_removesAndMovesBackToOpen() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId());
        GameEvent rejected = gameEventService.rejectChallenger(alice, e.getId());

        assertEquals(EventStatus.OPEN, rejected.getStatus());
        assertEquals(1, rejected.getParticipants().size());
        assertEquals(alice.getId(), rejected.getParticipants().get(0).getId());
    }

    @Test
    void rejectChallenger_byNonCreator_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId());
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.rejectChallenger(bob, e.getId()));
    }

    @Test
    void rejectChallenger_anotherChallengerCanApplyAfterRejection() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId());
        gameEventService.rejectChallenger(alice, e.getId());

        User charlie = userService.registerUser("charlie2_gs", "pass123", "charlie2_gs@test.com");
        charlie = userRepository.findByUsername("charlie2_gs");
        GameEvent e2 = gameEventService.acceptChallenge(charlie, e.getId());
        assertEquals(EventStatus.PENDING_APPROVAL, e2.getStatus());
        assertEquals(charlie.getId(), e2.getChallenger().getId());
    }

    // ── cancelEvent ──────────────────────────────────────────────────────────

    @Test
    void cancelEvent_byCreator_statusCancelled() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.cancelEvent(alice, e.getId());
        assertEquals(EventStatus.CANCELLED, gameEventService.getEvent(e.getId()).getStatus());
    }

    @Test
    void cancelEvent_byChallenger_works() {
        GameEvent e = createAndApprove();
        gameEventService.cancelEvent(bob, e.getId());
        assertEquals(EventStatus.CANCELLED, gameEventService.getEvent(e.getId()).getStatus());
    }

    @Test
    void cancelEvent_nonParticipant_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.cancelEvent(bob, e.getId()));
    }

    @Test
    void cancelEvent_finishedEvent_throws() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null);
        gameEventService.reportResult(bob,   e.getId(), alice.getUsername(), null);
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.cancelEvent(alice, e.getId()));
    }

    // ── reportResult — agreement (win) ────────────────────────────────────────

    @Test
    void reportResult_bothAgreeOnWinner_statusFinishedScoresUpdated() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null);
        GameEvent finished = gameEventService.reportResult(bob, e.getId(), alice.getUsername(), null);

        assertEquals(EventStatus.FINISHED, finished.getStatus());
        assertEquals(alice.getId(), finished.getWinner().getId());

        User updatedAlice = userRepository.findByUsername("alice_gs");
        User updatedBob   = userRepository.findByUsername("bob_gs");
        assertEquals(1, updatedAlice.getWins());
        assertEquals(0, updatedAlice.getLosses());
        assertEquals(0, updatedBob.getWins());
        assertEquals(1, updatedBob.getLosses());
    }

    @Test
    void reportResult_challengerWins_scoresCorrect() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), bob.getUsername(), null);
        gameEventService.reportResult(bob,   e.getId(), bob.getUsername(), null);

        assertEquals(1, userRepository.findByUsername("bob_gs").getWins());
        assertEquals(1, userRepository.findByUsername("alice_gs").getLosses());
    }

    @Test
    void reportResult_secondSubmissionResolvesEvent() {
        GameEvent e = createAndApprove();
        // First submission — event still IN_PROGRESS
        GameEvent afterFirst = gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null);
        assertEquals(EventStatus.IN_PROGRESS, afterFirst.getStatus());
        assertTrue(afterFirst.isCreatorResultSubmitted());
        assertFalse(afterFirst.isChallengerResultSubmitted());

        // Second submission — event resolves
        GameEvent afterSecond = gameEventService.reportResult(bob, e.getId(), alice.getUsername(), null);
        assertEquals(EventStatus.FINISHED, afterSecond.getStatus());
    }

    @Test
    void reportResult_resultNoteStoredOnAgreement() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), "won 3-0");
        GameEvent finished = gameEventService.reportResult(bob, e.getId(), alice.getUsername(), null);
        assertEquals("won 3-0", finished.getResultNote());
    }

    // ── reportResult — agreement (draw) ──────────────────────────────────────

    @Test
    void reportResult_bothAgreeOnDraw_drawScoresUpdated() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), null, null);
        GameEvent finished = gameEventService.reportResult(bob, e.getId(), null, null);

        assertEquals(EventStatus.FINISHED, finished.getStatus());
        assertNull(finished.getWinner());
        assertEquals(1, userRepository.findByUsername("alice_gs").getDraws());
        assertEquals(1, userRepository.findByUsername("bob_gs").getDraws());
    }

    @Test
    void reportResult_blankStringAlsoCountsAsDraw() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), "   ", null);
        GameEvent finished = gameEventService.reportResult(bob, e.getId(), "", null);
        assertNull(finished.getWinner());
        assertEquals(EventStatus.FINISHED, finished.getStatus());
    }

    // ── reportResult — disagreement (disputed) ────────────────────────────────

    @Test
    void reportResult_disagreement_statusDisputed() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null); // alice says she won
        GameEvent disputed = gameEventService.reportResult(bob, e.getId(), bob.getUsername(), null); // bob says he won

        assertEquals(EventStatus.DISPUTED, disputed.getStatus());
    }

    @Test
    void reportResult_disagreement_noScoreChanges() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null);
        gameEventService.reportResult(bob,   e.getId(), bob.getUsername(),   null);

        User updatedAlice = userRepository.findByUsername("alice_gs");
        User updatedBob   = userRepository.findByUsername("bob_gs");
        assertEquals(0, updatedAlice.getWins());
        assertEquals(0, updatedAlice.getLosses());
        assertEquals(0, updatedBob.getWins());
        assertEquals(0, updatedBob.getLosses());
    }

    @Test
    void reportResult_disagreement_incrementsDisputeCounterForBoth() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null);
        gameEventService.reportResult(bob,   e.getId(), bob.getUsername(),   null);

        assertEquals(1, userRepository.findByUsername("alice_gs").getDisputes());
        assertEquals(1, userRepository.findByUsername("bob_gs").getDisputes());
    }

    @Test
    void reportResult_winVsDraw_isDisputed() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null); // alice says win
        GameEvent disputed = gameEventService.reportResult(bob, e.getId(), null, null); // bob says draw
        assertEquals(EventStatus.DISPUTED, disputed.getStatus());
    }

    // ── reportResult — guard cases ────────────────────────────────────────────

    @Test
    void reportResult_onOpenEvent_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null));
    }

    @Test
    void reportResult_onPendingApprovalEvent_throws() {
        GameEvent e = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(bob, e.getId());
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null));
    }

    @Test
    void reportResult_nonParticipant_throws() {
        GameEvent e = createAndApprove();
        User charlie = userService.registerUser("charlie3_gs", "pass123", "charlie3_gs@test.com");
        charlie = userRepository.findByUsername("charlie3_gs");
        User finalC = charlie;
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.reportResult(finalC, e.getId(), alice.getUsername(), null));
    }

    @Test
    void reportResult_winnerNotAParticipant_throws() {
        GameEvent e = createAndApprove();
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.reportResult(alice, e.getId(), "nobody", null));
    }

    @Test
    void reportResult_submitTwiceByCreator_throws() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null);
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.reportResult(alice, e.getId(), alice.getUsername(), null));
    }

    @Test
    void reportResult_submitTwiceByChallenger_throws() {
        GameEvent e = createAndApprove();
        gameEventService.reportResult(bob, e.getId(), bob.getUsername(), null);
        assertThrows(IllegalArgumentException.class,
                () -> gameEventService.reportResult(bob, e.getId(), bob.getUsername(), null));
    }

    // ── getActiveEvents ───────────────────────────────────────────────────────

    @Test
    void getActiveEvents_includesOpen_PendingApproval_InProgress_excludesOthers() {
        GameEvent open        = gameEventService.createEvent(alice, 51.5, -0.1, GameType.TIC_TAC_TOE, FUTURE);

        User u2 = userService.registerUser("u2_gs", "pass123", "u2_gs@test.com");
        u2 = userRepository.findByUsername("u2_gs");
        GameEvent pending     = gameEventService.createEvent(alice, 52.0, -0.2, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(u2, pending.getId()); // PENDING_APPROVAL

        User u3 = userService.registerUser("u3_gs", "pass123", "u3_gs@test.com");
        u3 = userRepository.findByUsername("u3_gs");
        GameEvent inProgress  = gameEventService.createEvent(alice, 53.0, -0.3, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(u3, inProgress.getId());
        gameEventService.approveChallenger(alice, inProgress.getId()); // IN_PROGRESS

        GameEvent cancelled   = gameEventService.createEvent(alice, 54.0, -0.4, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.cancelEvent(alice, cancelled.getId());

        User u4 = userService.registerUser("u4_gs", "pass123", "u4_gs@test.com");
        u4 = userRepository.findByUsername("u4_gs");
        GameEvent finished    = gameEventService.createEvent(alice, 55.0, -0.5, GameType.TIC_TAC_TOE, FUTURE);
        gameEventService.acceptChallenge(u4, finished.getId());
        gameEventService.approveChallenger(alice, finished.getId());
        gameEventService.reportResult(alice, finished.getId(), alice.getUsername(), null);
        gameEventService.reportResult(u4,    finished.getId(), alice.getUsername(), null);

        List<GameEvent> active = gameEventService.getActiveEvents();
        assertTrue(active.stream().anyMatch(ev -> ev.getId().equals(open.getId())));
        assertTrue(active.stream().anyMatch(ev -> ev.getId().equals(pending.getId())));
        assertTrue(active.stream().anyMatch(ev -> ev.getId().equals(inProgress.getId())));
        assertFalse(active.stream().anyMatch(ev -> ev.getId().equals(cancelled.getId())));
        assertFalse(active.stream().anyMatch(ev -> ev.getId().equals(finished.getId())));
    }

    // ── getPlayerHistory ──────────────────────────────────────────────────────

    @Test
    void getPlayerHistory_returnsFinishedAndDisputed_notOthers() {
        GameEvent finished = createAndApprove();
        gameEventService.reportResult(alice, finished.getId(), alice.getUsername(), null);
        gameEventService.reportResult(bob,   finished.getId(), alice.getUsername(), null);

        User u5 = userService.registerUser("u5_gs", "pass123", "u5_gs@test.com");
        u5 = userRepository.findByUsername("u5_gs");
        GameEvent disputed = gameEventService.createEvent(alice, 52.0, -0.2, GameType.ROCK_PAPER_SCISSORS, FUTURE);
        gameEventService.acceptChallenge(u5, disputed.getId());
        gameEventService.approveChallenger(alice, disputed.getId());
        gameEventService.reportResult(alice, disputed.getId(), alice.getUsername(), null);
        gameEventService.reportResult(u5,    disputed.getId(), u5.getUsername(),    null);

        List<GameEvent> history = gameEventService.getPlayerHistory(alice);
        assertTrue(history.stream().anyMatch(e -> e.getId().equals(finished.getId())));
        assertTrue(history.stream().anyMatch(e -> e.getId().equals(disputed.getId())));
    }
}
