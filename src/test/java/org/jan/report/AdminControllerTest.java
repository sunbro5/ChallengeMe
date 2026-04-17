package org.jan.report;

import org.jan.BaseIntegrationTest;
import org.jan.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private MockHttpSession adminSession;
    private String regularUser;

    private String uid() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 9);
    }

    @BeforeEach
    void setUp() throws Exception {
        adminSession = loginAs("admin", "admin123");
        regularUser = uid();
        registerUser(regularUser, "pass123", regularUser + "@t.com");
    }

    @Test
    void getReports_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/admin/reports").session(adminSession))
                .andExpect(status().isOk());
    }

    @Test
    void getReports_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReports_asRegularUser_returns403() throws Exception {
        MockHttpSession userSession = loginAs(regularUser, "pass123");
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
        // Create a second user to be the target of the report
        String target = "target_" + uid();
        registerUser(target, "pass123", target + "@t.com");

        // regularUser submits a report against target
        MockHttpSession userSession = loginAs(regularUser, "pass123");
        mockMvc.perform(post("/reports")
                .session(userSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("reportedUsername", target, "reason", "test reason"))))
                .andExpect(status().isOk());

        // Fetch the report id from admin reports list
        MvcResult reportsResult = mockMvc.perform(get("/admin/reports").session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        List<?> reports = objectMapper.readValue(
                reportsResult.getResponse().getContentAsString(), List.class);
        Number reportId = (Number) reports.stream()
                .filter(r -> target.equals(((Map<?, ?>) r).get("reportedUsername")))
                .map(r -> ((Map<?, ?>) r).get("id"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/admin/resolve/" + reportId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string("Report resolved"));
    }

    @Test
    void submitReport_asRegularUser_works() throws Exception {
        MockHttpSession userSession = loginAs(regularUser, "pass123");
        String target = "t2_" + uid();
        registerUser(target, "pass123", target + "@t.com");

        mockMvc.perform(post("/reports")
                .session(userSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("reportedUsername", target, "reason", "Spamming"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Report submitted"));
    }
}
