package org.jan.report;

import org.jan.user.User;
import org.jan.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {

    private static final int MAX_REASON_LENGTH = 1000;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Report submitReport(User reporter, String reportedUsername, String reason) {
        if (reason == null || reason.trim().isEmpty())
            throw new IllegalArgumentException("Reason cannot be empty");
        if (reason.length() > MAX_REASON_LENGTH)
            throw new IllegalArgumentException("Reason cannot exceed " + MAX_REASON_LENGTH + " characters");
        User reported = userRepository.findByUsername(reportedUsername);
        if (reported == null) throw new IllegalArgumentException("User not found");
        if (reported.getId().equals(reporter.getId())) throw new IllegalArgumentException("Cannot report yourself");
        return reportRepository.save(new Report(reporter, reported, reason.trim()));
    }

    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Report resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setStatus(ReportStatus.RESOLVED);
        return reportRepository.save(report);
    }

    @Transactional
    public User banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if ("ADMIN".equals(user.getRole())) throw new IllegalArgumentException("Cannot ban an admin");
        user.setBanned(true);
        return userRepository.save(user);
    }

    @Transactional
    public User unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBanned(false);
        return userRepository.save(user);
    }
}
