package org.jan.game;

import org.jan.BaseIntegrationTest;
import org.jan.user.UserRepository;
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

class GameEventControllerTest extends BaseIntegrationTest {

    @Autowired private UserRepository      userRepository;
    @Autowired private GameEventRepository gameEventRepository;

    private MockHttpSession aliceSession;
    private MockHttpSession bobSession;
    private String aliceUsername;
    private String bobUsername;

    private String uid() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String scheduledAt() {
        return LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    /** POST /events and return the parsed response map. */
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
        aliceUsername = "alice_ec_" + uid();
        bobUsername   = "bob_ec_"   + uid();

        registerUser(aliceUsername, "pass123", aliceUsername + "@t.com");
        registerUser(bobUsername,   "pass123", bobUsername   + "@t.com");

        aliceSession = loginAs(aliceUsername, "pass123");
        bobSession   = loginAs(bobUsername,   "pass123");
    }

    // ── GET /events ───────────────────────────────────────────────────────────

    @Test
    void getActiveEvents_returnsOk() throws Exception {
        mockMvc.perform(get("/events").session(aliceSession)).andExpect(status().isOk());
    }

    @Test
    void getActiveEvents_containsCreatedEvent() throws Exception {
        Map<?, ?> created = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) created.get("id");

        MvcResult r = mockMvc.perform(get("/events").session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        List<?> list = objectMapper.readValue(r.getResponse().getContentAsString(), List.class);
        assertTrue(list.stream()
                .anyMatch(e -> id.equals(((Map<?, ?>) e).get("id"))));
    }

    // ── POST /events ──────────────────────────────────────────────────────────

    @Test
    void createEvent_noSession_returns401() throws Exception {
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "latitude", 51.5, "longitude", -0.1,
                        "gameType", "TIC_TAC_TOE", "scheduledAt", scheduledAt()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEvent_success_statusOpen_creatorJoined() throws Exception {
        Map<?, ?> body = createEvent(aliceSession, "ROCK_PAPER_SCISSORS");
        assertEquals("OPEN",                body.get("status"));
        assertEquals(aliceUsername,         body.get("creatorUsername"));
        assertEquals("ROCK_PAPER_SCISSORS", body.get("gameType"));
        assertTrue((Boolean) body.get("joined"));
        assertTrue((Boolean) body.get("isCreator"));
    }

    @Test
    void createEvent_overDailyLimit_returns400() throws Exception {
        for (int i = 0; i < GameEventService.DAILY_EVENT_LIMIT; i++) {
            mockMvc.perform(post("/events")
                    .session(aliceSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "latitude", 51.5 + i, "longitude", -0.1,
                            "gameType", "TIC_TAC_TOE", "scheduledAt", scheduledAt()))))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/events")
                .session(aliceSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "latitude", 60.0, "longitude", -0.1,
                        "gameType", "TIC_TAC_TOE", "scheduledAt", scheduledAt()))))
                .andExpect(status().isBadRequest());
    }

    // ── POST /events/{id}/accept ──────────────────────────────────────────────

    @Test
    void acceptChallenge_movesPendingApproval() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) event.get("id");

        MvcResult r = mockMvc.perform(post("/events/" + id + "/accept")
                .session(bobSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("PENDING_APPROVAL", body.get("status"));
        assertEquals(bobUsername, body.get("challengerUsername"));
    }

    @Test
    void acceptChallenge_noSession_returns401() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        mockMvc.perform(post("/events/" + event.get("id") + "/accept"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptChallenge_byCreator_returns400() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        mockMvc.perform(post("/events/" + event.get("id") + "/accept").session(aliceSession))
                .andExpect(status().isBadRequest());
    }

    // ── POST /events/{id}/approve ─────────────────────────────────────────────

    @Test
    void approveChallenger_byCreator_movesInProgress() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) event.get("id");
        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));

        MvcResult r = mockMvc.perform(post("/events/" + id + "/approve").session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("IN_PROGRESS", body.get("status"));
    }

    @Test
    void approveChallenger_byChallenger_returns400() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) event.get("id");
        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));

        mockMvc.perform(post("/events/" + id + "/approve").session(bobSession))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveChallenger_noSession_returns401() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) event.get("id");
        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));
        mockMvc.perform(post("/events/" + id + "/approve"))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /events/{id}/reject ──────────────────────────────────────────────

    @Test
    void rejectChallenger_byCreator_movesBackToOpen() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) event.get("id");
        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));

        MvcResult r = mockMvc.perform(post("/events/" + id + "/reject").session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("OPEN", body.get("status"));
        assertNull(body.get("challengerUsername"));
    }

    @Test
    void rejectChallenger_byChallenger_returns400() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) event.get("id");
        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));
        mockMvc.perform(post("/events/" + id + "/reject").session(bobSession))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /events/{id} ───────────────────────────────────────────────────

    @Test
    void cancelEvent_byCreator_returns200() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        mockMvc.perform(delete("/events/" + event.get("id")).session(aliceSession))
                .andExpect(status().isOk())
                .andExpect(content().string("Event cancelled"));
    }

    @Test
    void cancelEvent_noSession_returns401() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        mockMvc.perform(delete("/events/" + event.get("id")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelEvent_nonParticipant_returns400() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        mockMvc.perform(delete("/events/" + event.get("id")).session(bobSession))
                .andExpect(status().isBadRequest());
    }

    // ── POST /events/{id}/result ──────────────────────────────────────────────

    private Number createAndApprove() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        Number id = (Number) event.get("id");
        mockMvc.perform(post("/events/" + id + "/accept").session(bobSession));
        mockMvc.perform(post("/events/" + id + "/approve").session(aliceSession));
        return id;
    }

    @Test
    void reportResult_firstSubmission_stillInProgress() throws Exception {
        Number id = createAndApprove();
        MvcResult r = mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))))
                .andExpect(status().isOk()).andReturn();

        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("IN_PROGRESS", body.get("status"));
        assertTrue((Boolean) body.get("creatorResultSubmitted"));
        assertFalse((Boolean) body.get("challengerResultSubmitted"));
        assertTrue((Boolean) body.get("iHaveSubmitted"));
    }

    @Test
    void reportResult_bothAgree_statusFinished() throws Exception {
        Number id = createAndApprove();
        mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))));

        MvcResult r = mockMvc.perform(post("/events/" + id + "/result")
                .session(bobSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))))
                .andExpect(status().isOk()).andReturn();

        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("FINISHED", body.get("status"));
        assertEquals(aliceUsername, body.get("winnerUsername"));

        assertEquals(1, userRepository.findByUsername(aliceUsername).getWins());
        assertEquals(1, userRepository.findByUsername(bobUsername).getLosses());
    }

    @Test
    void reportResult_bothAgreeOnDraw_statusFinishedNoWinner() throws Exception {
        Number id = createAndApprove();
        mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", ""))));

        MvcResult r = mockMvc.perform(post("/events/" + id + "/result")
                .session(bobSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", ""))))
                .andExpect(status().isOk()).andReturn();

        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("FINISHED", body.get("status"));
        assertNull(body.get("winnerUsername"));
        assertEquals(1, userRepository.findByUsername(aliceUsername).getDraws());
        assertEquals(1, userRepository.findByUsername(bobUsername).getDraws());
    }

    @Test
    void reportResult_disagreement_statusDisputed() throws Exception {
        Number id = createAndApprove();
        mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))));

        MvcResult r = mockMvc.perform(post("/events/" + id + "/result")
                .session(bobSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", bobUsername))))
                .andExpect(status().isOk()).andReturn();

        Map<?, ?> body = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        assertEquals("DISPUTED", body.get("status"));
        assertEquals(0, userRepository.findByUsername(aliceUsername).getWins());
        assertEquals(0, userRepository.findByUsername(bobUsername).getWins());
        assertEquals(1, userRepository.findByUsername(aliceUsername).getDisputes());
        assertEquals(1, userRepository.findByUsername(bobUsername).getDisputes());
        assertNotNull(body.get("creatorResult"));
        assertNotNull(body.get("challengerResult"));
    }

    @Test
    void reportResult_noSession_returns401() throws Exception {
        Number id = createAndApprove();
        mockMvc.perform(post("/events/" + id + "/result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reportResult_onOpenEvent_returns400() throws Exception {
        Map<?, ?> event = createEvent(aliceSession, "TIC_TAC_TOE");
        mockMvc.perform(post("/events/" + event.get("id") + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportResult_submitTwice_returns400() throws Exception {
        Number id = createAndApprove();
        mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))));

        mockMvc.perform(post("/events/" + id + "/result")
                .session(aliceSession).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("winnerUsername", aliceUsername))))
                .andExpect(status().isBadRequest());
    }

    // ── GET /leaderboard ──────────────────────────────────────────────────────

    @Test
    void getLeaderboard_returnsOk_andContainsExpectedFields() throws Exception {
        MvcResult r = mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk()).andReturn();
        List<?> list = objectMapper.readValue(r.getResponse().getContentAsString(), List.class);
        if (!list.isEmpty()) {
            Map<?, ?> first = (Map<?, ?>) list.get(0);
            assertTrue(first.containsKey("username"));
            assertTrue(first.containsKey("wins"));
            assertTrue(first.containsKey("losses"));
            assertTrue(first.containsKey("draws"));
            assertTrue(first.containsKey("disputes"));
        }
    }
}
