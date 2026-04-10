package org.jan.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jan.BaseIntegrationTest;
import org.jan.user.LoginRequest;
import org.jan.user.RegisterRequest;
import org.jan.user.UserRepository;
import org.jan.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportService reportService;

    private String uid() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 9);
    }

    /** Login and return the MockHttpSession for subsequent requests. */
    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult r = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    private MockHttpSession adminSession;
    private String regularUser;

    @BeforeEach
    void setUp() throws Exception {
        adminSession = login("admin", "admin123");
        regularUser = uid();
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest(regularUser, "pass123", regularUser + "@t.com"))))
                .andExpect(status().isOk());
    }

    @Test
    void getReports_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/admin/reports").session(adminSession))
                .andExpect(status().isOk());
    }

    @Test
    void getReports_withoutSession_returns403() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReports_asRegularUser_returns403() throws Exception {
        MockHttpSession userSession = login(regularUser, "pass123");
        mockMvc.perform(get("/admin/reports").session(userSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void banAndUnban_asAdmin_works() throws Exception {
        Long userId = userRepository.findByUsername(regularUser).getId();

        mockMvc.perform(post("/admin/ban/" + userId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string("User banned"));

        mockMvc.perform(post("/admin/unban/" + userId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string("User unbanned"));
    }

    @Test
    void banAdmin_returns400() throws Exception {
        Long adminId = userRepository.findByUsername("admin").getId();
        mockMvc.perform(post("/admin/ban/" + adminId).session(adminSession))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot ban an admin"));
    }

    @Test
    void resolveReport_asAdmin_works() throws Exception {
        // Submit a report first
        var reporter = userRepository.findByUsername("admin");
        var reported = userRepository.findByUsername(regularUser);
        Report r = reportService.submitReport(reporter, regularUser, "test reason");

        mockMvc.perform(post("/admin/resolve/" + r.getId()).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string("Report resolved"));
    }

    @Test
    void submitReport_asRegularUser_works() throws Exception {
        MockHttpSession userSession = login(regularUser, "pass123");
        String target = uid();
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest(target, "pass123", target + "@t.com"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/reports")
                .session(userSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("reportedUsername", target, "reason", "Spamming"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Report submitted"));
    }
}
