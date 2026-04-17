package org.jan.user;

import org.jan.BaseIntegrationTest;
import org.jan.game.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
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

    @Autowired private UserRepository userRepository;

    private String aliceUsername;
    private String bobUsername;
    private MockHttpSession aliceSession;
    private MockHttpSession bobSession;

    private String uid() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String scheduledAt() {
        return LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    private Map<?, ?> createEvent(MockHttpSession session, String gameType) throws Exception {
        MvcResult r = mockMvc.perform(post("/events")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "latitude", 51.5, "longitude", -0.1,
                        "gameType", gameType, "scheduledAt", scheduledAt()))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
    }

    @BeforeEach
    void setUp() throws Exception {
        aliceUsername = "alice_pc_" + uid();
        bobUsername   = "bob_pc_"   + uid();

        registerUser(aliceUsername, "pass123", aliceUsername + "@t.com");
        registerUser(bobUsername,   "pass123", bobUsername   + "@t.com");

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
        mockMvc.perform(post("/friends/request?username=" + bobUsername).session(aliceSession));

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
        Map<?, ?> event = createEvent(aliceSession, GameType.TIC_TAC_TOE.name());
        Number id = (Number) event.get("id");

        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));
        mockMvc.perform(post("/events/" + id + "/approve").session(aliceSession));

        String winBody = objectMapper.writeValueAsString(
                Map.of("winnerUsername", winnerUsername == null ? "" : winnerUsername));
        mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON).content(winBody));
        mockMvc.perform(post("/events/" + id + "/result")
                .session(bobSession).contentType(MediaType.APPLICATION_JSON).content(winBody));
    }

    private void runDisputedGame() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, GameType.ROCK_PAPER_SCISSORS.name());
        Number id = (Number) event.get("id");

        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));
        mockMvc.perform(post("/events/" + id + "/approve").session(aliceSession));

        mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))));
        mockMvc.perform(post("/events/" + id + "/result")
                .session(bobSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", bobUsername))));
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
        runFullGame(null); // draw

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
