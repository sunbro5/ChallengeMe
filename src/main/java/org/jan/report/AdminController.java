package org.jan.report;

import org.jan.report.dto.ReportDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * All /admin/** endpoints are restricted to ROLE_ADMIN by SecurityConfig.
 * No manual role check needed here.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private ReportService reportService;

    @GetMapping("/reports")
    public ResponseEntity<List<ReportDto>> getReports() {
        List<ReportDto> dtos = reportService.getAllReports().stream()
                .map(r -> new ReportDto(
                        r.getId(),
                        r.getReporter().getUsername(),
                        r.getReported().getUsername(),
                        r.getReported().getId(),
                        r.getReported().isBanned(),
                        r.getReason(),
                        r.getCreatedAt().toString(),
                        r.getStatus().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/ban/{userId}")
    public ResponseEntity<String> banUser(@PathVariable Long userId) {
        try { reportService.banUser(userId); return ResponseEntity.ok("User banned"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/unban/{userId}")
    public ResponseEntity<String> unbanUser(@PathVariable Long userId) {
        try { reportService.unbanUser(userId); return ResponseEntity.ok("User unbanned"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/resolve/{reportId}")
    public ResponseEntity<String> resolveReport(@PathVariable Long reportId) {
        try { reportService.resolveReport(reportId); return ResponseEntity.ok("Report resolved"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }
}
