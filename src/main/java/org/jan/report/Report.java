package org.jan.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jan.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "reported_id")
    private User reported;

    private String reason;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    public Report(User reporter, User reported, String reason) {
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
        this.status = ReportStatus.PENDING;
    }
}
