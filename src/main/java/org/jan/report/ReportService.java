package org.jan.report;

import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public Report submitReport(User reporter, String reportedUsername, String reason) {
        if (reason == null || reason.trim().isEmpty())
            throw new IllegalArgumentException("Reason cannot be empty");
        User reported = userRepository.findByUsername(reportedUsername);
        if (reported == null) throw new IllegalArgumentException("User not found");
        if (reported.getId().equals(reporter.getId())) throw new IllegalArgumentException("Cannot report yourself");
        return reportRepository.save(new Report(reporter, reported, reason.trim()));
    }

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public Report resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setStatus(ReportStatus.RESOLVED);
        return reportRepository.save(report);
    }

    public User banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if ("ADMIN".equals(user.getRole())) throw new IllegalArgumentException("Cannot ban an admin");
        user.setBanned(true);
        return userRepository.save(user);
    }

    public User unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBanned(false);
        return userRepository.save(user);
    }
}
