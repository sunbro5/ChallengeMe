package org.jan.report;

import jakarta.servlet.http.HttpSession;
import org.jan.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class AdminController {

    @Autowired
    private ReportService reportService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Map<String, Object>>> getReports(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(reportService.getAllReports().stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "reporterUsername", r.getReporter().getUsername(),
                        "reportedUsername", r.getReported().getUsername(),
                        "reportedId", r.getReported().getId(),
                        "reportedBanned", r.getReported().isBanned(),
                        "reason", r.getReason(),
                        "createdAt", r.getCreatedAt().toString(),
                        "status", r.getStatus().name()))
                .collect(Collectors.toList()));
    }

    @PostMapping("/ban/{userId}")
    public ResponseEntity<String> banUser(@PathVariable Long userId, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Forbidden");
        try { reportService.banUser(userId); return ResponseEntity.ok("User banned"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/unban/{userId}")
    public ResponseEntity<String> unbanUser(@PathVariable Long userId, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Forbidden");
        try { reportService.unbanUser(userId); return ResponseEntity.ok("User unbanned"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/resolve/{reportId}")
    public ResponseEntity<String> resolveReport(@PathVariable Long reportId, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Forbidden");
        try { reportService.resolveReport(reportId); return ResponseEntity.ok("Report resolved"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }
}
