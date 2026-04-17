package org.jan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Abstract base for all integration tests.
 *
 * Spring caches the ApplicationContext when the configuration is identical across
 * subclasses, so the application starts ONCE per test suite run. The static
 * PostgreSQL container is shared by the same mechanism.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("challengeme_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("admin.username", () -> "admin");
        registry.add("admin.password", () -> "admin123");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Fetches a CAPTCHA, solves the arithmetic, and POSTs to /auth/register.
     * Supports: "a + b = ?", "a \u2212 b = ?", "a \u00D7 b = ?"
     */
    protected void registerUser(String username, String password, String email) throws Exception {
        MvcResult captchaResult = mockMvc.perform(get("/auth/captcha"))
                .andExpect(status().isOk())
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

        Map<String, Object> req = new HashMap<>();
        req.put("username", username);
        req.put("password", password);
        req.put("email", email);
        req.put("captchaId", captchaId);
        req.put("captchaAnswer", String.valueOf(answer));
        req.put("birthYear", 1995);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    /**
     * Logs in and returns the HTTP session.
     */
    protected MockHttpSession loginAs(String username, String password) throws Exception {
        Map<String, String> req = Map.of("username", username, "password", password);
        MvcResult r = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }
}
