package org.jan.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jan.BaseIntegrationTest;
import org.jan.user.LoginRequest;
import org.jan.user.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FriendControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession aliceSession;
    private MockHttpSession bobSession;
    private String aliceUsername;
    private String bobUsername;

    private String uid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private MockHttpSession loginAs(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        aliceUsername = "alice_" + uid();
        bobUsername   = "bob_"   + uid();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest(aliceUsername, "pass123", aliceUsername + "@test.com"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest(bobUsername, "pass123", bobUsername + "@test.com"))))
                .andExpect(status().isOk());

        aliceSession = loginAs(aliceUsername, "pass123");
        bobSession   = loginAs(bobUsername,   "pass123");
    }

    // ── /friends/request ─────────────────────────────────────────────────────

    @Test
    void sendRequest_success_returns200() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession))
                .andExpect(status().isOk())
                .andExpect(content().string("Request sent"));
    }

    @Test
    void sendRequest_noSession_returns401() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sendRequest_unknownUser_returns400() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", "nobody_at_all")
                .session(aliceSession))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendRequest_toSelf_returns400() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", aliceUsername)
                .session(aliceSession))
                .andExpect(status().isBadRequest());
    }

    // ── /friends/accept/{id} ──────────────────────────────────────────────────

    @Test
    void acceptRequest_success_returns200() throws Exception {
        MvcResult sendResult = mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession))
                .andExpect(status().isOk())
                .andReturn();

        // Retrieve the pending request id via Bob's pending list
        MvcResult pendingResult = mockMvc.perform(get("/friends/requests")
                .session(bobSession))
                .andExpect(status().isOk())
                .andReturn();

        List<?> pending = objectMapper.readValue(
                pendingResult.getResponse().getContentAsString(), List.class);
        assertEquals(1, pending.size());
        Number friendshipId = (Number) ((Map<?, ?>) pending.get(0)).get("id");

        mockMvc.perform(post("/friends/accept/" + friendshipId.longValue())
                .session(bobSession))
                .andExpect(status().isOk())
                .andExpect(content().string("Request accepted"));
    }

    @Test
    void acceptRequest_wrongUser_returns400() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession))
                .andExpect(status().isOk());

        MvcResult pendingResult = mockMvc.perform(get("/friends/requests")
                .session(bobSession))
                .andReturn();
        List<?> pending = objectMapper.readValue(
                pendingResult.getResponse().getContentAsString(), List.class);
        Number friendshipId = (Number) ((Map<?, ?>) pending.get(0)).get("id");

        // Alice (the requester) tries to accept her own request — should fail
        mockMvc.perform(post("/friends/accept/" + friendshipId.longValue())
                .session(aliceSession))
                .andExpect(status().isBadRequest());
    }

    // ── GET /friends ──────────────────────────────────────────────────────────

    @Test
    void getFriends_afterAccept_returnsBothSides() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession));

        MvcResult pendingResult = mockMvc.perform(get("/friends/requests")
                .session(bobSession)).andReturn();
        List<?> pending = objectMapper.readValue(
                pendingResult.getResponse().getContentAsString(), List.class);
        Number id = (Number) ((Map<?, ?>) pending.get(0)).get("id");
        mockMvc.perform(post("/friends/accept/" + id.longValue()).session(bobSession));

        MvcResult aliceFriends = mockMvc.perform(get("/friends").session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        List<?> list = objectMapper.readValue(
                aliceFriends.getResponse().getContentAsString(), List.class);
        assertEquals(1, list.size());
        assertEquals(bobUsername, ((Map<?, ?>) list.get(0)).get("username"));
    }

    @Test
    void getFriends_noSession_returns401() throws Exception {
        mockMvc.perform(get("/friends"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /friends/requests ─────────────────────────────────────────────────

    @Test
    void getPendingRequests_returnsIncomingOnly() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession));

        // Bob sees the request
        MvcResult bobResult = mockMvc.perform(get("/friends/requests").session(bobSession))
                .andExpect(status().isOk()).andReturn();
        List<?> bobPending = objectMapper.readValue(
                bobResult.getResponse().getContentAsString(), List.class);
        assertEquals(1, bobPending.size());
        assertEquals(aliceUsername, ((Map<?, ?>) bobPending.get(0)).get("requesterUsername"));

        // Alice sees nothing (she is the requester)
        MvcResult aliceResult = mockMvc.perform(get("/friends/requests").session(aliceSession))
                .andExpect(status().isOk()).andReturn();
        List<?> alicePending = objectMapper.readValue(
                aliceResult.getResponse().getContentAsString(), List.class);
        assertTrue(alicePending.isEmpty());
    }

    // ── DELETE /friends/with/{id} ─────────────────────────────────────────────

    @Test
    void unfriend_afterAccept_removesFriendship() throws Exception {
        // Send and accept request
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession));

        MvcResult pendingResult = mockMvc.perform(get("/friends/requests")
                .session(bobSession)).andReturn();
        List<?> pending = objectMapper.readValue(
                pendingResult.getResponse().getContentAsString(), List.class);
        Number id = (Number) ((Map<?, ?>) pending.get(0)).get("id");
        mockMvc.perform(post("/friends/accept/" + id.longValue()).session(bobSession));

        // Get Bob's id from Alice's friends list
        MvcResult friendsResult = mockMvc.perform(get("/friends").session(aliceSession))
                .andReturn();
        List<?> friends = objectMapper.readValue(
                friendsResult.getResponse().getContentAsString(), List.class);
        Number bobId = (Number) ((Map<?, ?>) friends.get(0)).get("id");

        // Alice unfriends Bob
        mockMvc.perform(delete("/friends/with/" + bobId.longValue())
                .session(aliceSession))
                .andExpect(status().isOk())
                .andExpect(content().string("Unfriended"));

        // Friends list should now be empty for both
        MvcResult aliceAfter = mockMvc.perform(get("/friends").session(aliceSession)).andReturn();
        assertTrue(objectMapper.readValue(
                aliceAfter.getResponse().getContentAsString(), List.class).isEmpty());

        MvcResult bobAfter = mockMvc.perform(get("/friends").session(bobSession)).andReturn();
        assertTrue(objectMapper.readValue(
                bobAfter.getResponse().getContentAsString(), List.class).isEmpty());
    }

    @Test
    void unfriend_unknownUser_returns400() throws Exception {
        mockMvc.perform(delete("/friends/with/999999")
                .session(aliceSession))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unfriend_noSession_returns401() throws Exception {
        mockMvc.perform(delete("/friends/with/1"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /friends/search ───────────────────────────────────────────────────

    @Test
    void search_matchingUsername_returnsResult() throws Exception {
        // bobUsername starts with "bob_"; search for the unique suffix portion
        String query = bobUsername.substring(4); // strip "bob_" prefix → unique uid part
        MvcResult result = mockMvc.perform(get("/friends/search")
                .param("q", query)
                .session(aliceSession))
                .andExpect(status().isOk())
                .andReturn();

        List<?> results = objectMapper.readValue(
                result.getResponse().getContentAsString(), List.class);
        assertEquals(1, results.size());
        Map<?, ?> entry = (Map<?, ?>) results.get(0);
        assertEquals(bobUsername, entry.get("username"));
        assertEquals("NONE", entry.get("friendStatus"));
    }

    @Test
    void search_noSession_returns401() throws Exception {
        mockMvc.perform(get("/friends/search").param("q", "bob"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void search_queryTooShort_returnsEmpty() throws Exception {
        MvcResult result = mockMvc.perform(get("/friends/search")
                .param("q", "a")
                .session(aliceSession))
                .andExpect(status().isOk())
                .andReturn();
        List<?> results = objectMapper.readValue(
                result.getResponse().getContentAsString(), List.class);
        assertTrue(results.isEmpty());
    }

    @Test
    void search_excludesSelf() throws Exception {
        // Search alice's own unique suffix — should not appear in results
        String query = aliceUsername.substring(6); // strip "alice_" prefix
        MvcResult result = mockMvc.perform(get("/friends/search")
                .param("q", query)
                .session(aliceSession))
                .andExpect(status().isOk())
                .andReturn();
        List<?> results = objectMapper.readValue(
                result.getResponse().getContentAsString(), List.class);
        assertTrue(results.stream()
                .noneMatch(r -> aliceUsername.equals(((Map<?, ?>) r).get("username"))));
    }

    @Test
    void search_showsPendingStatusAfterRequest() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession))
                .andExpect(status().isOk());

        String query = bobUsername.substring(4);
        MvcResult result = mockMvc.perform(get("/friends/search")
                .param("q", query)
                .session(aliceSession))
                .andExpect(status().isOk())
                .andReturn();

        List<?> results = objectMapper.readValue(
                result.getResponse().getContentAsString(), List.class);
        assertEquals(1, results.size());
        assertEquals("PENDING", ((Map<?, ?>) results.get(0)).get("friendStatus"));
    }

    @Test
    void search_showsAcceptedStatusAfterAccept() throws Exception {
        mockMvc.perform(post("/friends/request")
                .param("username", bobUsername)
                .session(aliceSession));

        MvcResult pendingResult = mockMvc.perform(get("/friends/requests")
                .session(bobSession)).andReturn();
        List<?> pending = objectMapper.readValue(
                pendingResult.getResponse().getContentAsString(), List.class);
        Number id = (Number) ((Map<?, ?>) pending.get(0)).get("id");
        mockMvc.perform(post("/friends/accept/" + id.longValue()).session(bobSession));

        String query = bobUsername.substring(4);
        MvcResult result = mockMvc.perform(get("/friends/search")
                .param("q", query)
                .session(aliceSession))
                .andExpect(status().isOk())
                .andReturn();

        List<?> results = objectMapper.readValue(
                result.getResponse().getContentAsString(), List.class);
        assertEquals(1, results.size());
        assertEquals("ACCEPTED", ((Map<?, ?>) results.get(0)).get("friendStatus"));
    }

    @Test
    void search_excludesAdminUsers() throws Exception {
        // Admin is seeded by AdminInitializer; searching "admin" should return nothing
        MvcResult result = mockMvc.perform(get("/friends/search")
                .param("q", "admin")
                .session(aliceSession))
                .andExpect(status().isOk())
                .andReturn();
        List<?> results = objectMapper.readValue(
                result.getResponse().getContentAsString(), List.class);
        assertTrue(results.stream()
                .noneMatch(r -> "ADMIN".equals(((Map<?, ?>) r).get("role"))));
    }
}
