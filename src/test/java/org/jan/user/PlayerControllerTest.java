package org.jan.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jan.BaseIntegrationTest;
import org.jan.game.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlayerControllerTest extends BaseIntegrationTest {

    @Autowired private MockMvc          mockMvc;
    @Autowired private ObjectMapper     objectMapper;
    @Autowired private UserRepository   userRepository;
    @Autowired private GameEventService gameEventService;

    private String aliceUsername;
    private String bobUsername;
    private MockHttpSession aliceSession;
    private MockHttpSession bobSession;

    private String uid() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private MockHttpSession loginAs(String username, String password) throws Exception {
        MvcResult r = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        aliceUsername = "alice_pc_" + uid();
        bobUsername   = "bob_pc_"   + uid();

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest(aliceUsername, "pass123", aliceUsername + "@t.com"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest(bobUsername, "pass123", bobUsername + "@t.com"))))
                .andExpect(status().isOk());

        aliceSession = loginAs(aliceUsername, "pass123");
        bobSession   = loginAs(bobUsername,   "pass123");
    }

    // ── GET /players/{username} ───────────────────────────────────────────────

    @Test
    void getProfile_existingUser_returns200WithStats() throws Exception {
        MvcResult r = mockMvc.perform(get("/players/" + aliceUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();

        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals(aliceUsername, body.get("username"));
        assertTrue(body.containsKey("wins"));
        assertTrue(body.containsKey("losses"));
        assertTrue(body.containsKey("draws"));
        assertTrue(body.containsKey("disputes"));
        assertTrue(body.containsKey("games"));
        assertTrue((Boolean) body.get("isMe"));
    }

    @Test
    void getProfile_viewingOtherUser_isMeFalse() throws Exception {
        MvcResult r = mockMvc.perform(get("/players/" + bobUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertFalse((Boolean) body.get("isMe"));
    }

    @Test
    void getProfile_unknownUser_returns404() throws Exception {
        mockMvc.perform(get("/players/nobody_xyz_123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_adminUser_returns404() throws Exception {
        mockMvc.perform(get("/players/admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_noSession_stillReturnsPublicProfile() throws Exception {
        mockMvc.perform(get("/players/" + aliceUsername))
                .andExpect(status().isOk());
    }

    // ── Friend status in profile ──────────────────────────────────────────────

    @Test
    void getProfile_friendStatusNoneByDefault() throws Exception {
        MvcResult r = mockMvc.perform(get("/players/" + bobUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("NONE", body.get("friendStatus"));
    }

    @Test
    void getProfile_friendStatusPendingAfterRequest() throws Exception {
        mockMvc.perform(post("/friends/request?username=" + bobUsername)
                .session(aliceSession));

        MvcResult r = mockMvc.perform(get("/players/" + bobUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("PENDING", body.get("friendStatus"));
    }

    @Test
    void getProfile_friendStatusAcceptedAfterBothConfirm() throws Exception {
        // Alice sends request
        mockMvc.perform(post("/friends/request?username=" + bobUsername).session(aliceSession));
        // Bob accepts
        MvcResult requestsResult = mockMvc.perform(get("/friends/requests").session(bobSession))
                .andReturn();
        List<?> requests = objectMapper.readValue(
                requestsResult.getResponse().getContentAsString(), List.class);
        Number friendshipId = (Number) ((Map<?, ?>) requests.get(0)).get("id");
        mockMvc.perform(post("/friends/accept/" + friendshipId).session(bobSession));

        MvcResult r = mockMvc.perform(get("/players/" + bobUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("ACCEPTED", body.get("friendStatus"));
    }

    @Test
    void getProfile_noSession_friendStatusNone() throws Exception {
        MvcResult r = mockMvc.perform(get("/players/" + aliceUsername))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("NONE", body.get("friendStatus"));
    }

    // ── Game history in profile ───────────────────────────────────────────────

    private void runFullGame(String winnerUsername) throws Exception {
        // Alice creates, Bob accepts, Alice approves, both submit same result
        User alice = userRepository.findByUsername(aliceUsername);
        User bob   = userRepository.findByUsername(bobUsername);

        GameEvent e = gameEventService.createEvent(
                alice, 51.5, -0.1, GameType.TIC_TAC_TOE,
                LocalDateTime.now().plusDays(1));
        gameEventService.acceptChallenge(bob, e.getId());
        gameEventService.approveChallenger(alice, e.getId());
        gameEventService.reportResult(alice, e.getId(), winnerUsername, null);
        gameEventService.reportResult(bob,   e.getId(), winnerUsername, null);
    }

    private void runDisputedGame() throws Exception {
        User alice = userRepository.findByUsername(aliceUsername);
        User bob   = userRepository.findByUsername(bobUsername);

        GameEvent e = gameEventService.createEvent(
                alice, 52.0, -0.2, GameType.ROCK_PAPER_SCISSORS,
                LocalDateTime.now().plusDays(1));
        gameEventService.acceptChallenge(bob, e.getId());
        gameEventService.approveChallenger(alice, e.getId());
        gameEventService.reportResult(alice, e.getId(), aliceUsername, null); // alice says she won
        gameEventService.reportResult(bob,   e.getId(), bobUsername,   null); // bob says he won
    }

    @Test
    void getProfile_showsWonGameInHistory() throws Exception {
        runFullGame(aliceUsername);

        MvcResult r = mockMvc.perform(get("/players/" + aliceUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        List<?> games = (List<?>) body.get("games");

        assertEquals(1, games.size());
        Map<?, ?> game = (Map<?, ?>) games.get(0);
        assertEquals("won",          game.get("result"));
        assertEquals("FINISHED",     game.get("status"));
        assertEquals(bobUsername,    game.get("opponentUsername"));
        assertEquals("TIC_TAC_TOE", game.get("gameType"));
    }

    @Test
    void getProfile_showsLostGameInHistory() throws Exception {
        runFullGame(aliceUsername); // bob lost

        MvcResult r = mockMvc.perform(get("/players/" + bobUsername).session(bobSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        List<?> games  = (List<?>) body.get("games");

        assertEquals(1, games.size());
        assertEquals("lost", ((Map<?, ?>) games.get(0)).get("result"));
    }

    @Test
    void getProfile_showsDrawInHistory() throws Exception {
        runFullGame(null); // draw (null winner)

        MvcResult r = mockMvc.perform(get("/players/" + aliceUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        List<?> games  = (List<?>) body.get("games");

        assertEquals("draw", ((Map<?, ?>) games.get(0)).get("result"));
    }

    @Test
    void getProfile_showsDisputedGameInHistory() throws Exception {
        runDisputedGame();

        MvcResult r = mockMvc.perform(get("/players/" + aliceUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        List<?> games  = (List<?>) body.get("games");

        assertEquals(1, games.size());
        Map<?, ?> game = (Map<?, ?>) games.get(0);
        assertEquals("disputed", game.get("result"));
        assertEquals("DISPUTED", game.get("status"));
        // Both claimed results exposed
        assertNotNull(game.get("creatorResult"));
        assertNotNull(game.get("challengerResult"));
    }

    @Test
    void getProfile_statsReflectHistory() throws Exception {
        runFullGame(aliceUsername); // alice wins
        runFullGame(null);          // draw

        MvcResult r = mockMvc.perform(get("/players/" + aliceUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals(1, body.get("wins"));
        assertEquals(0, body.get("losses"));
        assertEquals(1, body.get("draws"));
        assertEquals(0, body.get("disputes"));
    }

    @Test
    void getProfile_disputeCountReflected() throws Exception {
        runDisputedGame();

        MvcResult r = mockMvc.perform(get("/players/" + aliceUsername).session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals(1, body.get("disputes"));
        assertEquals(0, body.get("wins"));
    }
}
