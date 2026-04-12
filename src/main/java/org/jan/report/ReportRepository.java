package org.jan.report;

import org.jan.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByOrderByCreatedAtDesc();
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByReporterOrReported(User reporter, User reported);
}
