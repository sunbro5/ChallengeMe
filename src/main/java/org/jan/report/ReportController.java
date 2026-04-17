package org.jan.report;

import org.jan.report.dto.SubmitReportRequest;
import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private ReportService  reportService;
    @Autowired private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> submitReport(
            @RequestBody SubmitReportRequest req, Authentication auth) {
        User reporter = userRepository.findByUsername(auth.getName());
        try {
            reportService.submitReport(reporter, req.getReportedUsername(), req.getReason());
            return ResponseEntity.ok("Report submitted");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
