package org.jan.report;

import jakarta.servlet.http.HttpSession;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> submitReport(@RequestBody Map<String, String> body, HttpSession session) {
        User s = (User) session.getAttribute("user");
        if (s == null) return ResponseEntity.status(401).body("Not logged in");
        User reporter = userRepository.findById(s.getId()).orElse(null);
        if (reporter == null) return ResponseEntity.status(401).body("Not logged in");
        try {
            reportService.submitReport(reporter, body.get("reportedUsername"), body.get("reason"));
            return ResponseEntity.ok("Report submitted");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
