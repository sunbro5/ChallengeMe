package org.jan.user;

import org.jan.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseIntegrationTest {

    private String uid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    @Test
    void register_validInput_returns200() throws Exception {
        String u = uid();
        registerUser(u, "pass123", u + "@test.com");
    }

    @Test
    void register_duplicateUsername_returns400() throws Exception {
        String u = uid();
        registerUser(u, "pass123", u + "@test.com");

        // Second registration with same username must fail — use a raw POST with a fresh captcha
        // but registerUser() expects success; instead we submit with wrong/empty captcha which we
        // know will be rejected before the duplicate check.  Use a direct helper that allows
        // non-200 responses:
        MvcResult captchaResult = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/captcha"))
                .andReturn();
        Map<?, ?> captcha = objectMapper.readValue(
                captchaResult.getResponse().getContentAsString(), Map.class);
        String captchaId = (String) captcha.get("id");
        String question  = ((String) captcha.get("question")).replace(" = ?", "");
        int answer;
        if (question.contains(" + ")) {
            String[] p = question.split(" \\+ ");
            answer = Integer.parseInt(p[0].trim()) + Integer.parseInt(p[1].trim());
        } else if (question.contains(" \u2212 ")) {
            String[] p = question.split(" \u2212 ");
            answer = Integer.parseInt(p[0].trim()) - Integer.parseInt(p[1].trim());
        } else {
            String[] p = question.split(" \u00D7 ");
            answer = Integer.parseInt(p[0].trim()) * Integer.parseInt(p[1].trim());
        }

        java.util.Map<String, Object> req = new java.util.HashMap<>();
        req.put("username", u);
        req.put("password", "pass123");
        req.put("email", "other@test.com");
        req.put("captchaId", captchaId);
        req.put("captchaAnswer", String.valueOf(answer));
        req.put("birthYear", 1995);

        mockMvc.perform(post("/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        MvcResult captchaResult = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/captcha"))
                .andReturn();
        Map<?, ?> captcha = objectMapper.readValue(
                captchaResult.getResponse().getContentAsString(), Map.class);
        String captchaId = (String) captcha.get("id");
        String question  = ((String) captcha.get("question")).replace(" = ?", "");
        int answer;
        if (question.contains(" + ")) {
            String[] p = question.split(" \\+ ");
            answer = Integer.parseInt(p[0].trim()) + Integer.parseInt(p[1].trim());
        } else if (question.contains(" \u2212 ")) {
            String[] p = question.split(" \u2212 ");
            answer = Integer.parseInt(p[0].trim()) - Integer.parseInt(p[1].trim());
        } else {
            String[] p = question.split(" \u00D7 ");
            answer = Integer.parseInt(p[0].trim()) * Integer.parseInt(p[1].trim());
        }

        java.util.Map<String, Object> req = new java.util.HashMap<>();
        req.put("username", uid());
        req.put("password", "123");
        req.put("email", uid() + "@test.com");
        req.put("captchaId", captchaId);
        req.put("captchaAnswer", String.valueOf(answer));
        req.put("birthYear", 1995);

        mockMvc.perform(post("/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returnsUsernameAndRole() throws Exception {
        String u = uid();
        registerUser(u, "pass123", u + "@test.com");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(u, "pass123"))))
                .andExpect(status().isOk())
                .andReturn();

        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        assertEquals(u, body.get("username"));
        assertEquals("USER", body.get("role"));
    }

    @Test
    void login_wrongPassword_returns400() throws Exception {
        String u = uid();
        registerUser(u, "pass123", u + "@test.com");

        mockMvc.perform(post("/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(u, "wrong"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void login_storesUserInSession() throws Exception {
        String u = uid();
        registerUser(u, "pass123", u + "@test.com");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(u, "pass123"))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        User sessionUser = (User) session.getAttribute("user");
        assertNotNull(sessionUser);
        assertEquals(u, sessionUser.getUsername());
    }

    @Test
    void logout_invalidatesSession() throws Exception {
        String u = uid();
        registerUser(u, "pass123", u + "@test.com");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(u, "pass123"))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));

        assertTrue(session.isInvalid());
    }
}
