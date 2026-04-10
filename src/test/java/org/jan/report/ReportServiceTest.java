package org.jan.report;

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
class ReportServiceTest extends BaseIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        userService.registerUser("alice_r", "pass123", "alice_r@example.com");
        userService.registerUser("bob_r",   "pass123", "bob_r@example.com");
        alice = userRepository.findByUsername("alice_r");
        bob   = userRepository.findByUsername("bob_r");
    }

    @Test
    void submitReport_success_createsPendingReport() {
        Report r = reportService.submitReport(alice, "bob_r", "Spam");
        assertNotNull(r.getId());
        assertEquals(ReportStatus.PENDING, r.getStatus());
        assertEquals("alice_r", r.getReporter().getUsername());
        assertEquals("bob_r", r.getReported().getUsername());
        assertEquals("Spam", r.getReason());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void submitReport_emptyReason_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.submitReport(alice, "bob_r", "  "));
    }

    @Test
    void submitReport_unknownUser_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.submitReport(alice, "nobody", "reason"));
    }

    @Test
    void submitReport_self_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.submitReport(alice, "alice_r", "self report"));
    }

    @Test
    void resolveReport_changesPendingToResolved() {
        Report r = reportService.submitReport(alice, "bob_r", "Cheating");
        Report resolved = reportService.resolveReport(r.getId());
        assertEquals(ReportStatus.RESOLVED, resolved.getStatus());
    }

    @Test
    void resolveReport_unknownId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> reportService.resolveReport(999999L));
    }

    @Test
    void getAllReports_returnsNewestFirst() {
        reportService.submitReport(alice, "bob_r", "First");
        reportService.submitReport(bob, "alice_r", "Second");
        List<Report> reports = reportService.getAllReports();
        assertTrue(reports.size() >= 2);
        // Newest should be first
        assertTrue(
            reports.get(0).getCreatedAt().isAfter(reports.get(reports.size() - 1).getCreatedAt())
            || reports.get(0).getCreatedAt().isEqual(reports.get(reports.size() - 1).getCreatedAt())
        );
    }

    @Test
    void banUser_setsUserBanned() {
        reportService.banUser(bob.getId());
        User updated = userRepository.findById(bob.getId()).orElseThrow();
        assertTrue(updated.isBanned());
    }

    @Test
    void banUser_admin_throws() {
        User admin = userRepository.findByUsername("admin");
        assertNotNull(admin);
        assertThrows(IllegalArgumentException.class,
                () -> reportService.banUser(admin.getId()));
    }

    @Test
    void unbanUser_clearsIsBanned() {
        reportService.banUser(bob.getId());
        reportService.unbanUser(bob.getId());
        User updated = userRepository.findById(bob.getId()).orElseThrow();
        assertFalse(updated.isBanned());
    }
}
